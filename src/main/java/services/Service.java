package services;

import io.grpc.ManagedChannel;

public interface Service {

    void listen(ManagedChannel channel);

    void start();

    void stop();
}
