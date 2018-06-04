package services;

import com.google.protobuf.TextFormat;
import core.Config;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import jobs.VideoJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import utils.ChannelUtil;
import utils.SimpleStreamObserver;

import javax.net.ssl.SSLException;

public class WebrtcClientService implements Service {
    private static Logger logger = LoggerFactory.getLogger(WebrtcClientService.class);

    @Override
    public void listen(ManagedChannel channel) {
        WebrtcGrpc.WebrtcStub stub = WebrtcGrpc.newStub(channel);
        stub.webrtc(WebrtcInfo.newBuilder().build(), new StreamObserver<WebrtcResponse>() {
            @Override
            public void onNext(WebrtcResponse value) {
                logger.info(TextFormat.shortDebugString(value));
                if (value.getObject().equals("bicycle") || value.getObject().equals("person")) {
                    try {
                        ManagedChannel managementChannel =
                                ChannelUtil.getInstance().getClientChannel(Config.MANAGEMENT_IP, Config.PORT);
                        JobMonitorGrpc.JobMonitorStub stub = JobMonitorGrpc.newStub(managementChannel);
                        stub.finish(Job.newBuilder().setId(12345).setName(jobs.Job.getJobName(VideoJob.class)).build(),
                                new SimpleStreamObserver<Empty>() {
                                    @Override
                                    public void onNext(Empty value) {
                                    }
                                });
                    } catch (SSLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
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
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
    }
}
