package services;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Empty;
import proto.hermes.RandomGeneratorGrpc;
import proto.hermes.RandomNumber;
import utils.ChannelUtil;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomGenerator extends RandomGeneratorGrpc.RandomGeneratorImplBase implements Service {
    private static final AtomicInteger atomicInteger = new AtomicInteger();
    private static Logger logger = LoggerFactory.getLogger(RandomGenerator.class);
    private final int index = atomicInteger.getAndAdd(1);
    long value;
    private StreamObserver<RandomNumber> observer;
    private Random random = new Random();
    private volatile boolean interrupted;

    @Override
    public void generate(Empty request, StreamObserver<RandomNumber> responseObserver) {
        observer = responseObserver;
    }

    @Override
    public void stop() {
        interrupted = true;
    }

    @Override
    public void start() {
        logger.info("Start random generator");
        interrupted = false;
        ChannelUtil.getInstance().execute(this::run, 1000);
    }

    private void run() {
        if (!interrupted) {
            if (observer != null) {
                observer.onNext(RandomNumber.newBuilder().setValue(index * 100 + value++).build());
            }
            ChannelUtil.getInstance().execute(this::run, 1000);
        } else {
            if (observer != null) {
                observer.onCompleted();
                observer = null;
            }
        }
    }
}
