package services;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;
import proto.hermes.Protocol;

public interface Service extends BindableService {

    default void listen(ManagedChannel channel) {
    }

    /**
     * Initiate the service, usually start some background threads.
     */
    default void init() {
    }

    /**
     * Start the service, usually make a RPC call to the remotePeer.
     */
    default void start() {
    }

    /**
     * Stop the service, terminate all workers
     */
    default void stop() {
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
