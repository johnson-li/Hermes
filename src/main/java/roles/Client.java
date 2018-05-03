package roles;

import core.Context;
import jobs.VideoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.*;
import services.Service;

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
        addServices(new WebrtcClientService());
        super.init();
    }

    public void initJob() {
        JobManagerGrpc.JobManagerBlockingStub stub = JobManagerGrpc.newBlockingStub(getChannel());
        InitJobResult result =
                stub.initJob(Job.newBuilder().setName(jobs.Job.getJobName(VideoJob.class)).build());
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
}
