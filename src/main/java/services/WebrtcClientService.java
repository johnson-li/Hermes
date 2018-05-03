package services;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Protocol;
import proto.hermes.WebrtcGrpc;
import proto.hermes.WebrtcInfo;
import proto.hermes.WebrtcResponse;

public class WebrtcClientService implements Service {
    private static Logger logger = LoggerFactory.getLogger(WebrtcClientService.class);

    @Override
    public void listen(ManagedChannel channel) {
        WebrtcGrpc.WebrtcStub stub = WebrtcGrpc.newStub(channel);
        stub.webrtc(WebrtcInfo.newBuilder().build(), new StreamObserver<WebrtcResponse>() {
            @Override
            public void onNext(WebrtcResponse value) {
                logger.info(value.getStatus().name());
            }

            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                logger.info("Completed");
            }
        });
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

    @Override
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
    }
}
