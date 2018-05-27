package services;

import io.grpc.stub.StreamObserver;
import proto.hermes.HeartbeatGrpc;
import proto.hermes.HeartbeatRequest;
import proto.hermes.HeartbeatResponse;
import proto.hermes.Status;

@DefaultRun
public class HeartbeatService extends HeartbeatGrpc.HeartbeatImplBase implements Service {
    private HeartbeatListener listener;

    public HeartbeatService(HeartbeatListener listener) {
        this.listener = listener;
    }

    @Override
    public void beat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        responseObserver.onNext(HeartbeatResponse.newBuilder().setStatus(Status.SUCCESS).build());
        listener.onHeartbeat(request.getParticipantId());
    }

    public interface HeartbeatListener {
        void onHeartbeat(long participantId);
    }
}
