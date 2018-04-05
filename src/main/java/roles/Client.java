package roles;

import core.Context;
import jobs.EchoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.PrintService;
import services.Service;
import services.TaskController;
import services.TaskListener;

public class Client extends Role implements TaskListener {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private long jobId;

    public Client(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(new PrintService());
        super.init();
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
}
