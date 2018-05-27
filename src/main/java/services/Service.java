package services;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Protocol;

public interface Service extends BindableService {
    Logger logger = LoggerFactory.getLogger(Service.class);

    default void listen(ManagedChannel channel) {
        logger.info("Listen to remote service");
    }

    /**
     * Initiate the service, usually start some background threads.
     */
    default void init() {
        logger.info("Init service: " + getClass().getSimpleName());
    }

    /**
     * Start the service, usually make a RPC call to the remotePeer.
     */
    default void start() {
        logger.info("Start service: " + getClass().getSimpleName());
    }

    /**
     * Stop the service, terminate all workers
     */
    default void stop() {
        logger.info("Stop service: " + getClass().getSimpleName());
    }

    /**
     * Initiate the listener, as a service server
     *
     * @return gRPC service stub
     */
    @Override
    default ServerServiceDefinition bindService() {
        return null;
    }

    default String getName() {
        return getClass().getSimpleName();
    }

    default proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.GRPC).build();
    }
}
