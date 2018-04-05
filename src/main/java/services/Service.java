package services;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ServerServiceDefinition;

public interface Service extends BindableService {

    void listen(ManagedChannel channel);

    /**
     * Initiate the service, usually start some background threads.
     */
    void init();

    /**
     * Start the service, usually make a RPC call to the remotePeer.
     */
    void start();

    /**
     * Stop the service, terminate all workers
     */
    void stop();

    /**
     * Initiate the listener, as a service server
     *
     * @return gRPC service stub
     */
    @Override
    default ServerServiceDefinition bindService() {
        return null;
    }
}
