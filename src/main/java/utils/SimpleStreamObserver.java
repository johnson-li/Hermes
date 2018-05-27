package utils;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleStreamObserver<T> implements StreamObserver<T> {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    @Override
    public void onCompleted() {
    }
}
