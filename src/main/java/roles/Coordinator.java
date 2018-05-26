package roles;

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
    private Map<Long, AtomicBoolean> servicesStartTag = new HashMap<>();

    public Coordinator(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new RegistrationService(this), new JobService(this),
                new HeartbeatService(this), new WebrtcServer(), new ServiceRegistration(this));
        super.init();
    }

    @Override
    public void onRegistered(RegistrationRequest request) {
        Participant participant = request.getParticipant();
        participants.put(participant.getId(), participant);
    }

    @Override
    public void stopServices(Job job) {
        jobs.Job workingJob = jobs.get(job.getId());
        List<Task> tasks = workingJob.getTasks();
        ChannelUtil.getInstance().execute(() ->
                tasks.forEach(this::notifyParticipantStop));
    }

    @Override
    public void initServices(Job job) {
        logger.info("init services: " + job.getId());
        jobs.get(job.getId()).getTasks().forEach(task -> {
        });
    }

    @Override
    public void startServices(Job job) {
        logger.info("start services command: " + job.getId());
        servicesStartTag.put(job.getId(), new AtomicBoolean(true));
        AtomicBoolean flag = servicesStartTag.get(job.getId());
        if (flag.compareAndSet(true, false) && servicesByParticipant.getOrDefault(job.getId(), new ArrayList<>()).size() >= 3) {
            startServices(job.getId());
        }
    }

    private void startServices(long jobId) {
        logger.info("start services: " + jobId);
        jobs.Job workingJob = jobs.get(jobId);
        List<Task> tasks = workingJob.getTasks();
        ChannelUtil.getInstance().execute(() -> tasks.forEach(task -> notifyParticipantStart(task, jobId)));
    }

    @Override
    public jobs.Job buildTasks(Job job) {
        jobs.Job workingJob = JobManager.getInstance().getByName(job.getName());
        List<Task> tasks = workingJob.getTasks().stream().map(this::assignParticipant).collect(Collectors.toList());
        workingJob.setTasks(tasks);
//        ChannelUtil.getInstance().execute(() -> tasks.forEach(this::notifyParticipant));
        servicesByJob.put(workingJob.getID(), new ArrayList<>());
//        ChannelUtil.getInstance().execute(() -> tasks.forEach(task -> startContainer(task, job.getId())));
        ChannelUtil.getInstance().execute(() -> startContainers(tasks, job.getId()));
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

    private void notifyParticipantStop(Task task) {
        try {
            ManagedChannel channel = ChannelUtil.getInstance()
                    .newClientChannel(task.getSelf().getAddress().getIp(), task.getSelf().getAddress().getPort());
            TaskControllerGrpc.TaskControllerBlockingStub stub = TaskControllerGrpc.newBlockingStub(channel);
            StopResult result = stub.stop(task);
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void notifyParticipantInit(Task task, long jobId) {
        try {
            ManagedChannel channel = ChannelUtil.getInstance()
                    .newClientChannel(task.getSelf().getAddress().getIp(), Config.SERVICE_PORT);
            ServiceControllerGrpc.ServiceControllerBlockingStub stub = ServiceControllerGrpc.newBlockingStub(channel);
            InitServiceResponse response = stub.init(InitServiceRequest.newBuilder()
                    .setIngressIP(task.getIngresses(0).getAddress().getIp())
                    .setIngressPort(Config.SERVICE_PORT).setEgressIP(task.getEgress().getAddress().getIp())
                    .setEgressPort(Config.SERVICE_PORT).build());
            logger.info("Service init response: " + response.getStatus().toString());
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
                    .newClientChannel(task.getSelf().getAddress().getIp(), Config.SERVICE_PORT);
            ServiceControllerGrpc.ServiceControllerBlockingStub stub = ServiceControllerGrpc.newBlockingStub(channel);
            StartServiceResponse result = stub.start(Empty.newBuilder().build());
            logger.info("Service start response: " + result.getStatus().toString());
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
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
            Map<String, String> data = DockerManager.getInstance().startContainerData(participant.getAddress().getIp(),
                    env, "johnson163/hermes", Config.SERVICE_PORT, participant.getRoles(0).getRole());
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

    private void notifyParticipant(Task task) {
        try {
            ManagedChannel channel = ChannelUtil.getInstance()
                    .newClientChannel(task.getSelf().getAddress().getIp(), task.getSelf().getAddress().getPort());
            TaskControllerGrpc.TaskControllerBlockingStub stub = TaskControllerGrpc.newBlockingStub(channel);
            AssignResult result = stub.assign(task);
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onHeartbeat(long participantId) {
//        logger.info("Received hearth beat from: " + participantId);
    }

    @Override
    public void onRegister(ServiceInfo serviceInfo) {
        logger.info("On service registered: " + serviceInfo.getNameList() + ", from: " + serviceInfo.getId());
        servicesByParticipant.computeIfAbsent(serviceInfo.getId(), (id) -> new ArrayList<>());
        servicesByParticipant.get(serviceInfo.getId()).add(serviceInfo);
        servicesByJob.get(serviceInfo.getJobId()).add(serviceInfo);
        if (servicesStartTag.getOrDefault(serviceInfo.getJobId(), new AtomicBoolean()).compareAndSet(true, false)
                && servicesByParticipant.get(serviceInfo.getJobId()).size() >= 3) {
            startServices(serviceInfo.getJobId());
        }
    }
}
