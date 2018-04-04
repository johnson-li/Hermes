package services;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import utils.ChannelUtil;

import java.util.concurrent.ConcurrentLinkedDeque;

public class EchoService extends EchoGrpc.EchoImplBase implements Service {

    private StreamObserver<EchoResponse> observer;
    private Logger logger = LoggerFactory.getLogger(EchoService.class);
    private ConcurrentLinkedDeque<Long> dataList = new ConcurrentLinkedDeque<>();
    private boolean interrupted = false;

    @Override
    public void stop() {
        interrupted = true;
        observer = null;
    }

    @Override
    public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
        observer = responseObserver;
    }

    @Override
    public void listen(ManagedChannel channel) {
        RandomGeneratorGrpc.RandomGeneratorStub stub = RandomGeneratorGrpc.newStub(channel);
        stub.generate(Empty.newBuilder().build(), new StreamObserver<RandomNumber>() {
            @Override
            public void onNext(RandomNumber value) {
                dataList.addLast(value.getValue());
            }

            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                if (observer != null) {
                    observer = null;
                }
            }
        });
    }

    @Override
    public void start() {
        interrupted = false;
        ChannelUtil.getInstance().execute(this::run, 2000);
    }

    private void run() {
        if (!interrupted) {
            if (observer != null) {
                String str = "";
                while (!dataList.isEmpty()) {
                    Long val = dataList.pollFirst();
                    str += val + ", ";
                }
                observer.onNext(EchoResponse.newBuilder().setMsg(str).build());
                ChannelUtil.getInstance().execute(this::run, 2000);
            }
        }
    }
}
