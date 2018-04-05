package services;

import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.EchoGrpc;
import proto.hermes.EchoRequest;
import proto.hermes.EchoResponse;

public class PrintService implements Service {
    private static Logger logger = LoggerFactory.getLogger(PrintService.class);

    @Override
    public void start() {

    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void listen(ManagedChannel channel) {
        EchoGrpc.EchoStub stub = EchoGrpc.newStub(channel);
        stub.echo(EchoRequest.newBuilder().build(), new StreamObserver<EchoResponse>() {
            @Override
            public void onNext(EchoResponse value) {
                logger.info("Print message: " + value.getMsg());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                logger.info("Completed");
            }
        });
    }
}
