package roles;

import core.Context;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import jobs.EchoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.PrintService;
import services.TaskController;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;
import java.util.List;

public class Client extends Role implements TaskController.TaskListener {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private PrintService printService = new PrintService();
    private long jobId;

    public Client(Context context, BindableService... services) {
        super(context, services);
    }

    @Override
    public void init() {
        super.init();
        addServices(new TaskController(this));
    }

    public void initJob() {
        JobManagerGrpc.JobManagerBlockingStub stub = JobManagerGrpc.newBlockingStub(getChannel());
        InitJobResult result =
                stub.initJob(Job.newBuilder().setName(jobs.Job.getJobName(EchoJob.class)).build());
        jobId = result.getId();
    }

    public void startJob() {
        JobManagerGrpc.JobManagerBlockingStub stub = JobManagerGrpc.newBlockingStub(getChannel());
        StartJobResult result = stub.startJob(Job.newBuilder().setId(jobId).build());
    }

    public void finishJob() {
        JobManagerGrpc.JobManagerBlockingStub stub = JobManagerGrpc.newBlockingStub(getChannel());
        FinishJobResult result = stub.finishJob(Job.newBuilder().setId(jobId).build());
    }

    @Override
    public void onStopped(Task task) {

    }

    @Override
    public void onStarted(Task task) {

    }

    @Override
    public void onTaskAssigned(Task task) {
        List<Participant> ingresses = task.getIngressesList();
        for (Participant ingress : ingresses) {
            try {
                ManagedChannel channel = ChannelUtil.getInstance().newClientChannel(ingress.getAddress().getIp(),
                        ingress.getAddress().getPort());
                String serviceName = task.getService().getName();
                ChannelUtil.getInstance().getWorkingGroup()
                        .execute(() -> getServiceManager().getService(serviceName).listen(channel));
            } catch (SSLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
