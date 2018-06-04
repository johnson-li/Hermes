package roles;

import com.google.common.base.Preconditions;
import com.google.protobuf.TextFormat;
import core.Config;
import core.Context;
import io.grpc.ManagedChannel;
import jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.*;
import services.Service;
import utils.ChannelUtil;
import utils.DockerManager;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Coordinator extends Role implements RegistrationService.RegistrationListener, JobService.JobListener,
        HeartbeatService.HeartbeatListener, ServiceListener {
    private static Logger logger = LoggerFactory.getLogger(Coordinator.class);
    private Map<Long, Participant> participants = new HashMap<>();
    private Map<Long, jobs.Job> jobs = new HashMap<>();
    private Map<Long, List<ServiceInfo>> servicesByParticipant = new HashMap<>();
    private Map<Long, List<ServiceInfo>> servicesByJob = new HashMap<>();
    private Map<Long, AtomicBoolean> servicesInitTag = new HashMap<>();
    private ServiceRegistration serviceRegistration = new ServiceRegistration(this);
    private JobService jobService = new JobService(this);

    public Coordinator(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new RegistrationService(this), jobService,
                new HeartbeatService(this), new WebrtcServer(), serviceRegistration);
        super.init();
    }

    @Override
    public void onRegistered(RegistrationRequest request) {
        Participant participant = request.getParticipant();
        participants.put(participant.getId(), participant);
    }

    @Override
    public void stopServices(long jobId) {
        jobs.Job workingJob = jobs.get(jobId);
        List<Task> tasks = workingJob.getTasks();
//        ChannelUtil.getInstance().execute(() ->
//                tasks.forEach(this::notifyParticipantStop));
        ChannelUtil.getInstance().execute(() ->
                tasks.forEach(this::stopContainers));
    }

    @Override
    public void initServices(long jobId) {
        logger.info("init services command: " + jobId);
        servicesInitTag.put(jobId, new AtomicBoolean(true));
        AtomicBoolean flag = servicesInitTag.get(jobId);
        if (servicesByJob.getOrDefault(jobId, new ArrayList<>()).size() >= 3) {
            if (flag.compareAndSet(true, false)) {
                initServices0(jobId);
            }
        }
    }

    private void initServices0(long jobId) {
        logger.info("init services: " + jobId);
        jobs.get(jobId).getTasks().forEach(task -> notifyParticipantInit(task, jobId));
        jobService.finishInitJob(jobId);
    }

    @Override
    public void startServices(long jobId) {
        logger.info("start services: " + jobId);
        jobs.Job workingJob = jobs.get(jobId);
        List<Task> tasks = workingJob.getTasks();
        ChannelUtil.getInstance().execute(() -> {
            tasks.stream().filter(task -> task.getSelf().getRoles(0).getRole().equals(Consumer.class.getSimpleName())).findAny().ifPresent(task -> notifyParticipantStart(task, jobId));
            tasks.stream().filter(task -> task.getSelf().getRoles(0).getRole().equals(Client.class.getSimpleName())).findAny().ifPresent(task -> notifyParticipantStart(task, jobId));
            tasks.stream().filter(task -> task.getSelf().getRoles(0).getRole().equals(Producer.class.getSimpleName())).findAny().ifPresent(task -> notifyParticipantStart(task, jobId));
        });
    }

    @Override
    public jobs.Job buildTasks(Job job) {
        jobs.Job workingJob = JobManager.getInstance().getByName(job.getName());
        List<Task> tasks = workingJob.getTasks().stream().map(this::assignParticipant).collect(Collectors.toList());
        tasks.forEach(task -> logger.info("Job task: " + TextFormat.shortDebugString(task)));
        workingJob.setTasks(tasks);
//        ChannelUtil.getInstance().execute(() -> tasks.forEach(this::notifyParticipant));
        servicesByJob.put(workingJob.getID(), new ArrayList<>());
//        ChannelUtil.getInstance().execute(() -> tasks.forEach(task -> startContainer(task, job.getId())));
        ChannelUtil.getInstance().execute(() -> startContainers(tasks, workingJob.getID()));
        jobs.put(workingJob.getID(), workingJob);
        logger.info(TextFormat.shortDebugString(workingJob.getTasks().get(0)));
        return workingJob;
    }

    private Task assignParticipant(Task task) {
        Task.Builder builder = Task.newBuilder();
        if (task.hasService()) {
            builder.setService(task.getService());
        }
        if (task.hasOperation()) {
            builder.setOperation(task.getOperation());
        }
        Participant self = Participant.newBuilder().mergeFrom(getParticipant(task.getSelf().getRoles(0))).build();
        builder.setSelf(self);
        if (task.getIngressesCount() > 0) {
            for (Participant oldIngress : task.getIngressesList()) {
                Participant ingress = Participant.newBuilder().mergeFrom(getParticipant(oldIngress.getRoles(0))).build();
                builder.addIngresses(ingress);
            }
        }
        if (task.hasEgress()) {
            Participant egress = Participant.newBuilder().mergeFrom(getParticipant(task.getEgress().getRoles(0))).build();
            builder.setEgress(egress);
        }
        return builder.build();
    }

    private Participant getParticipant(proto.hermes.Role role) {
        return participants.values().stream().filter(participant -> participant.getRolesList().stream().anyMatch(r ->
                r.getRole().toLowerCase().equals(role.getRole().toLowerCase()))).findFirst().orElse(null);
    }
//
//    private void notifyParticipantStop(Task task) {
//        try {
//            ManagedChannel channel = ChannelUtil.getInstance()
//                    .getClientChannel(task.getSelf().getAddress().getIp(), task.getSelf().getAddress().getPort());
//            TaskControllerGrpc.TaskControllerBlockingStub stub = TaskControllerGrpc.newBlockingStub(channel);
//            StopResult result = stub.stop(task);
//        } catch (SSLException e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    private void notifyParticipantInit(Task task, long jobId) {
        logger.info("Notify participant init: " + TextFormat.shortDebugString(task));
        Preconditions.checkNotNull(task);
        try {
            ServiceInfo info = servicesByJob.get(jobId).stream().filter(serviceInfo ->
                    serviceInfo.getNameList().get(0).toLowerCase().equals(task.getService().getName().toLowerCase()))
                    .findFirst().orElse(null);
            String serviceIP = info.getAddress().getIp();
            int servicePort = info.getAddress().getPort();
            ManagedChannel channel = ChannelUtil.getInstance().getClientChannel(serviceIP, servicePort);
            ServiceControllerGrpc.ServiceControllerBlockingStub stub = ServiceControllerGrpc.newBlockingStub(channel);
            InitServiceRequest.Builder builder = InitServiceRequest.newBuilder();
            if (task.getIngressesCount() > 0) {
                ServiceInfo ingressInfo = servicesByParticipant.get(task.getIngresses(0).getId()).stream()
                        .filter(serviceInfo -> serviceInfo.getJobId() == jobId).findFirst().get();
                builder.setIngress(NetAddress.newBuilder().setIp(ingressInfo.getAddress().getIp())
                        .setPort(ingressInfo.getAddress().getPort()).build());
            }
            if (task.getEgress() != null && task.getEgress().getId() != 0) {
                ServiceInfo egressInfo = servicesByParticipant.get(task.getEgress().getId()).stream()
                        .filter(serviceInfo -> serviceInfo.getJobId() == jobId).findFirst().get();
                builder.setEgress(NetAddress.newBuilder().setIp(egressInfo.getAddress().getIp())
                        .setPort(egressInfo.getAddress().getPort()).build());
            }
            InitServiceRequest request = builder.build();
            logger.info("Init service request: " + TextFormat.shortDebugString(request));
            InitServiceResponse response = stub.init(request);
            logger.info("Service init response: " + TextFormat.shortDebugString(response));
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void notifyParticipantStart(Task task, long jobId) {
        try {
            String service = task.getService().getName();
            ServiceInfo info = servicesByJob.get(jobId).stream().filter(serviceInfo ->
                    serviceInfo.getName(0).equals(service)).findFirst().orElse(null);
            ManagedChannel channel = ChannelUtil.getInstance()
                    .getClientChannel(info.getAddress().getIp(), info.getAddress().getPort());
            ServiceControllerGrpc.ServiceControllerBlockingStub stub = ServiceControllerGrpc.newBlockingStub(channel);
            StartServiceResponse result = stub.start(Empty.newBuilder().build());
            logger.info("Service start response: " + result.getStatus().toString() + ", on " + TextFormat.shortDebugString(task.getSelf()));
        } catch (SSLException e) {
            logger.error(e.getMessage() + task, e);
        }
    }

    private void stopContainers(Task task) {

    }

    private void startContainers(List<Task> tasks, long jobId) {
        Map<String, List<Map<String, String>>> map = new HashMap<>();
        map.put("consumer", new ArrayList<>());
        map.put("producer", new ArrayList<>());
        tasks.forEach(task -> {
            Participant participant = task.getSelf();
            Map<String, String> env = new HashMap<>();
            env.put("functionality", "service");
            env.put("services", String.join(",", task.getSelf().getServicesList().stream().map(proto.hermes.Service::getName).collect(Collectors.toList())));
            env.put("job_id", Long.toString(jobId));
            env.put("host", task.getSelf().getAddress().getIp()); // The service and the management has the same IP address to the outside
            String image = "";
            if (participant.getRoles(0).getRole().equals(Client.class.getSimpleName())) {
                image = "johnson163/hermes-arm";
            } else if (participant.getRoles(0).getRole().equals(Producer.class.getSimpleName())) {
                image = "johnson163/hermes";
            } else if (participant.getRoles(0).getRole().equals(Consumer.class.getSimpleName())) {
                image = "johnson163/hermes-cuda";
            }
            Map<String, String> data = DockerManager.getInstance().startContainerData(participant.getAddress().getIp(),
                    env, image, Config.SERVICE_PORT, participant.getRoles(0).getRole());
            String type = participant.getRoles(0).getRole().toLowerCase();
            if (!type.equals("consumer")) {
                type = "producer";
            }
            map.get(type).add(data);
        });
        DockerManager.getInstance().startContainer(map);
    }

//    private void startContainer(Task task, long jobId) {
//        Participant participant = task.getSelf();
//        Map<String, String> env = new HashMap<>();
//        env.put("functionality", "service");
//        env.put("services", String.join(",", task.getSelf().getServicesList().stream().map(proto.hermes.Service::getName).collect(Collectors.toList())));
//        env.put("job_id", Long.toString(jobId));
//        DockerManager.getInstance().startContainerData(participant.getAddress().getIp(), env, "johnson163/hermes", Config.SERVICE_PORT, participant.getRoles(0).getRole());
//    }
//
//    private void notifyParticipant(Task task) {
//        try {
//            ManagedChannel channel = ChannelUtil.getInstance()
//                    .getClientChannel(task.getSelf().getAddress().getIp(), task.getSelf().getAddress().getPort());
//            TaskControllerGrpc.TaskControllerBlockingStub stub = TaskControllerGrpc.newBlockingStub(channel);
//            AssignResult result = stub.assign(task);
//        } catch (SSLException e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    @Override
    public void onHeartbeat(long participantId) {
//        logger.info("Received hearth beat from: " + participantId);
    }

    @Override
    public void onRegister(ServiceInfo serviceInfo) {
        logger.info("On service registered: " + TextFormat.shortDebugString(serviceInfo));
        servicesByParticipant.computeIfAbsent(serviceInfo.getId(), (id) -> new ArrayList<>());
        servicesByParticipant.get(serviceInfo.getId()).add(serviceInfo);
        servicesByJob.get(serviceInfo.getJobId()).add(serviceInfo);
        if (servicesByJob.get(serviceInfo.getJobId()).size() >= 3) {
            if (servicesInitTag.getOrDefault(serviceInfo.getJobId(), new AtomicBoolean()).compareAndSet(true, false)) {
                initServices0(serviceInfo.getJobId());
            }
        }
    }
}
