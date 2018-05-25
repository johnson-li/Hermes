package services;

import io.grpc.stub.StreamObserver;
import proto.hermes.*;

public class ServiceController extends ServiceControllerGrpc.ServiceControllerImplBase implements Service {

    @Override
    public void init(InitServiceRequest request, StreamObserver<InitServiceResponse> responseObserver) {
    }

    @Override
    public void start(Empty request, StreamObserver<StartServiceResponse> responseObserver) {
    }

    @Override
    public void stop(Empty request, StreamObserver<StopServiceResponse> responseObserver) {
    }
}
