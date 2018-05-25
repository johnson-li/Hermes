package services;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.HeartbeatGrpc;
import proto.hermes.HeartbeatRequest;
import proto.hermes.HeartbeatResponse;
import utils.ChannelUtil;

public class HeartbeatClient implements Service, StreamObserver<HeartbeatResponse> {
    private static Logger logger = LoggerFactory.getLogger(HeartbeatClient.class);
    private final long participantId;
    private volatile boolean interrupted;
    private HeartbeatGrpc.HeartbeatStub stub;


    public HeartbeatClient(long participantId) {
        this.participantId = participantId;
    }

    /**
     * The life cycle of HeartbeatClient is not the same as other services since it belongs to the participant directly.
     */
    @Override
    public void listen(ManagedChannel channel) {
        stub = HeartbeatGrpc.newStub(channel);
    }

    private void run() {
        if (!interrupted) {
            stub.beat(HeartbeatRequest.newBuilder().setParticipantId(participantId).build(), this);
            ChannelUtil.getInstance().execute(this::run, 1000);
        }
    }

    @Override
    public void start() {
        interrupted = false;
        ChannelUtil.getInstance().execute(this::run, 1000);
    }

    @Override
    public void stop() {
        interrupted = true;
    }

    @Override
    public void onNext(HeartbeatResponse value) {
//        logger.info("Heartbeat result: " + value.getStatus());
    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    @Override
    public void onCompleted() {
    }
}
