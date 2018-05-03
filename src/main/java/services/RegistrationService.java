package services;

import com.google.protobuf.TextFormat;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.RegistrationGrpc;
import proto.hermes.RegistrationRequest;
import proto.hermes.RegistrationResult;
import proto.hermes.Status;

@DefaultRun
public class RegistrationService extends RegistrationGrpc.RegistrationImplBase implements Service {
    private static Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private RegistrationListener listener;

    public RegistrationService(RegistrationListener listener) {
        this.listener = listener;
    }

    @Override
    public void register(RegistrationRequest request, StreamObserver<RegistrationResult> responseObserver) {
        logger.info("Register: " + TextFormat.shortDebugString(request));
        listener.onRegistered(request);
        responseObserver.onNext(RegistrationResult.newBuilder().setStatus(Status.SUCCESS)
                .setParticipantId(request.getParticipant().getId()).build());
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

    public interface RegistrationListener {
        void onRegistered(RegistrationRequest request);
    }
}
