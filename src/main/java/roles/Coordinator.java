package roles;

import com.google.protobuf.TextFormat;
import core.Context;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import jobs.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.HeartbeatService;
import services.JobService;
import services.RegistrationService;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Coordinator extends Role implements RegistrationService.RegistrationListener, JobService.JobListener,
        HeartbeatService.HeartbeatListener {
    private static Logger logger = LoggerFactory.getLogger(Coordinator.class);
    private List<Participant> participants = new ArrayList<>();
    private Map<String, Integer> participantIndexes = new HashMap<>();
    private Map<Long, jobs.Job> jobs = new HashMap<>();

    public Coordinator(Context context, BindableService... services) {
        super(context, services);
    }

    @Override
    public void init() {
        super.init();
        addServices(new RegistrationService(this));
        addServices(new JobService(this));
        addServices(new HeartbeatService(this));
    }

    @Override
    public void onRegistered(RegistrationRequest request) {
        participants.add(request.getParticipant());
    }

    @Override
    public void stopServices(Job job) {
        jobs.Job workingJob = jobs.get(job.getId());
        List<Task> tasks = workingJob.getTasks();
        ChannelUtil.getInstance().execute(() ->
                tasks.forEach(this::notifyParticipantStop));
    }

    @Override
    public void startServices(Job job) {
        jobs.Job workingJob = jobs.get(job.getId());
        List<Task> tasks = workingJob.getTasks();
        ChannelUtil.getInstance().execute(() ->
                tasks.forEach(this::notifyParticipantStart));
    }

    @Override
    public jobs.Job buildTasks(Job job) {
        jobs.Job workingJob = JobManager.getInstance().getByName(job.getName());
        List<Task> tasks = workingJob.getTasks().stream().map(this::assignParticipant).collect(Collectors.toList());
        workingJob.setTasks(tasks);
        ChannelUtil.getInstance().execute(() -> tasks.forEach(this::notifyParticipant));
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
        Participant self = Participant.newBuilder().mergeFrom(getParticipant(task.getSelf().getRoles(0), "self")).build();
        builder.setSelf(self);
        if (task.getIngressesCount() > 0) {
            for (Participant oldIngress : task.getIngressesList()) {
                Participant ingress = Participant.newBuilder().mergeFrom(getParticipant(oldIngress.getRoles(0), "ingress")).build();
                builder.addIngresses(ingress);
            }
        }
        if (task.hasEgress()) {
            Participant egress = Participant.newBuilder().mergeFrom(getParticipant(task.getEgress().getRoles(0), "egress")).build();
            builder.setEgress(egress);
        }
        return builder.build();
    }

    private Participant getParticipant(proto.hermes.Role role, String character) {
        int index = participantIndexes.getOrDefault(role.getRole() + character, 0);
        participantIndexes.put(role.getRole() + character, index + 1);
        if (character.equals("egress")) {
            index = 0;
        }
        return participants.stream().filter(participant -> participant.getRolesList().stream()
                .allMatch(r -> r.getRole().equals(role.getRole()))).collect(Collectors.toList()).get(index);
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

    private void notifyParticipantStart(Task task) {
        try {
            ManagedChannel channel = ChannelUtil.getInstance()
                    .newClientChannel(task.getSelf().getAddress().getIp(), task.getSelf().getAddress().getPort());
            TaskControllerGrpc.TaskControllerBlockingStub stub = TaskControllerGrpc.newBlockingStub(channel);
            StartResult result = stub.start(task);
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
    }

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

    }
}
