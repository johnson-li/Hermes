package roles;

import core.Context;
import jobs.EchoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.*;
import services.Service;
import utils.SimpleStreamObserver;

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

    public void initJob(JobListener jobListener) {
        logger.info("Init job");
        JobManagerGrpc.JobManagerStub stub = JobManagerGrpc.newStub(getChannel());
        stub.initJob(Job.newBuilder().setName(jobs.Job.getJobName(EchoJob.class)).build(), new SimpleStreamObserver<InitJobResult>() {
            @Override
            public void onNext(InitJobResult value) {
                jobId = value.getId();
                jobListener.onInit(Client.this, value);
            }
        });
    }

    public void startJob(JobListener jobListener) {
        logger.info("Start job");
        JobManagerGrpc.JobManagerStub stub = JobManagerGrpc.newStub(getChannel());
        stub.startJob(Job.newBuilder().setId(jobId).build(), new SimpleStreamObserver<StartJobResult>() {
            @Override
            public void onNext(StartJobResult result) {
                logger.info("Job start result: " + result.getStatus());
                jobListener.onStart(Client.this, result);
            }
        });
    }

    public void finishJob(JobListener jobListener) {
        logger.info("Finish job");
        JobManagerGrpc.JobManagerStub stub = JobManagerGrpc.newStub(getChannel());
        stub.finishJob(Job.newBuilder().setId(jobId).build(), new SimpleStreamObserver<FinishJobResult>() {
            @Override
            public void onNext(FinishJobResult result) {
                logger.info("Job stop result: " + result.getStatus());
                jobListener.onFinish(Client.this, result);
            }
        });
    }
}
