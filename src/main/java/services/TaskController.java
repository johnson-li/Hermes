package services;

import com.google.protobuf.TextFormat;
import exception.ServiceNotFoundException;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;

public class TaskController extends TaskControllerGrpc.TaskControllerImplBase implements Service {
    private static Logger logger = LoggerFactory.getLogger(TaskController.class);
    private TaskListener listener;

    public TaskController(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void stop(Task request, StreamObserver<StopResult> responseObserver) {
        logger.info("Task stopped: " + TextFormat.shortDebugString(request));
        listener.onStopped(request);
        responseObserver.onNext(StopResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void start(Task request, StreamObserver<StartResult> responseObserver) {
        logger.info("Task started: " + TextFormat.shortDebugString(request));
        try {
            listener.onStarted(request);
        } catch (ServiceNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        responseObserver.onNext(StartResult.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void assign(Task request, StreamObserver<AssignResult> responseObserver) {
        logger.info("Task assigned: " + TextFormat.shortDebugString(request));
        listener.onTaskAssigned(request);
        responseObserver.onNext(AssignResult.newBuilder().setStatus(Status.SUCCESS).build());
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
}
