package services;

import com.google.protobuf.TextFormat;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;

import java.util.HashMap;
import java.util.Map;

@DefaultRun
public class JobService extends JobManagerGrpc.JobManagerImplBase implements Service {
    private static Logger logger = LoggerFactory.getLogger(JobService.class);
    private Map<Long, StreamObserver<InitJobResult>> observerMap = new HashMap<>();

    private JobListener listener;

    public JobService(JobListener listener) {
        this.listener = listener;
    }

    public void finishInitJob(long id) {
        logger.info("Finish init job: " + id);
        StreamObserver<InitJobResult> observer = observerMap.remove(id);
        observer.onNext(InitJobResult.newBuilder().setId(id).setStatus(Status.SUCCESS).build());
        observer.onCompleted();
    }

    @Override
    public void finishJob(Job request, StreamObserver<FinishJobResult> responseObserver) {
        logger.info("Finish job: " + TextFormat.shortDebugString(request));
        listener.stopServices(request.getId());
        responseObserver.onNext(FinishJobResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void initJob(Job request, StreamObserver<InitJobResult> responseObserver) {
        logger.info("Init job: " + TextFormat.shortDebugString(request));
        jobs.Job job = listener.buildTasks(request);
        observerMap.put(job.getID(), responseObserver);
        listener.initServices(job.getID());
//        responseObserver.onNext(InitJobResult.newBuilder().setId(job.getID()).setStatus(Status.SUCCESS).build());
//        responseObserver.onCompleted();
    }

    @Override
    public void startJob(Job request, StreamObserver<StartJobResult> responseObserver) {
        logger.info("Start job: " + TextFormat.shortDebugString(request));
        listener.startServices(request.getId());
        responseObserver.onNext(StartJobResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    public interface JobListener {
        jobs.Job buildTasks(Job job);

        void initServices(long jobId);

        void startServices(long jobId);

        void stopServices(long jobId);
    }
}
