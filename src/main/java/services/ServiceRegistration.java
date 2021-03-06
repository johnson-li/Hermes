package services;

import io.grpc.stub.StreamObserver;
import proto.hermes.RegistrationResponse;
import proto.hermes.ServiceInfo;
import proto.hermes.ServiceRegistrationGrpc;
import proto.hermes.Status;

@DefaultRun
public class ServiceRegistration extends ServiceRegistrationGrpc.ServiceRegistrationImplBase implements Service {
    private final ServiceListener listener;

    public ServiceRegistration(ServiceListener listener) {
        this.listener = listener;
    }

    @Override
    public void registerService(ServiceInfo request, StreamObserver<RegistrationResponse> responseObserver) {
        listener.onRegister(request);
        responseObserver.onNext(RegistrationResponse.newBuilder().setStatus(Status.SUCCESS).build());
        responseObserver.onCompleted();
    }
}
