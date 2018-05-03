package services;

import com.google.protobuf.TextFormat;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;

@DefaultRun
public class JobService extends JobManagerGrpc.JobManagerImplBase implements Service {
    private static Logger logger = LoggerFactory.getLogger(JobService.class);

    private JobListener listener;

    public JobService(JobListener listener) {
        this.listener = listener;
    }

    @Override
    public void finishJob(Job request, StreamObserver<FinishJobResult> responseObserver) {
        logger.info("Finish job: " + TextFormat.shortDebugString(request));
        listener.stopServices(request);
        responseObserver.onNext(FinishJobResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void initJob(Job request, StreamObserver<InitJobResult> responseObserver) {
        logger.info("Init job: " + TextFormat.shortDebugString(request));
        listener.initServices(request);
        jobs.Job job = listener.buildTasks(request);
        responseObserver.onNext(InitJobResult.newBuilder().setId(job.getID()).setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void startJob(Job request, StreamObserver<StartJobResult> responseObserver) {
        logger.info("Start job: " + TextFormat.shortDebugString(request));
        listener.startServices(request);
        responseObserver.onNext(StartJobResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listen(ManagedChannel channel) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public interface JobListener {
        jobs.Job buildTasks(Job job);

        void initServices(Job job);

        void startServices(Job job);

        void stopServices(Job job);
    }
}
