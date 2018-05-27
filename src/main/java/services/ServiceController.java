package services;

import com.google.protobuf.TextFormat;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;

public class ServiceController extends ServiceControllerGrpc.ServiceControllerImplBase implements Service {
    private final ServiceControlListener listener;
    private Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public ServiceController(ServiceControlListener listener) {
        this.listener = listener;
    }

    @Override
    public void init(InitServiceRequest request, StreamObserver<InitServiceResponse> responseObserver) {
        logger.info("Init service: " + TextFormat.shortDebugString(request));
        listener.onInit(request);
        responseObserver.onNext(InitServiceResponse.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void start(Empty request, StreamObserver<StartServiceResponse> responseObserver) {
        listener.onStart();
        responseObserver.onNext(StartServiceResponse.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }

    @Override
    public void stop(Empty request, StreamObserver<StopServiceResponse> responseObserver) {
        logger.info("Stop service: " + getName());
        listener.onStop();
        responseObserver.onNext(StopServiceResponse.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }
}
