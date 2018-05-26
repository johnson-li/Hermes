package core;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.Service;
import services.ServiceController;
import services.ServiceManager;
import utils.ChannelUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceStarter {
    private Logger logger = LoggerFactory.getLogger(ServiceStarter.class);
    private ServiceManager serviceManager = new ServiceManager();

    public static void main(String[] args) throws Exception {
        new ServiceStarter().start();
    }

    public void start() throws Exception {
        logger.info("Starting...");

        // Create service
        List<Service> services = Config.SERVICES.stream().map(service ->
                serviceManager.getService(service)).collect(Collectors.toList());
        ServiceController controller = new ServiceController();

        // Start the server
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ServerBuilder builder = ServerBuilder.forPort(Config.SERVICE_PORT).useTransportSecurity(ssc.certificate(),
                ssc.privateKey()).addService(controller);
        services.stream().filter(service -> service.bindService() != null).forEach(builder::addService);
        Server server = builder.build().start();

        logger.info("Server started");
        // Get participant id
        ManagedChannel managementChannel =
                ChannelUtil.getInstance().newClientChannel(Config.MANAGEMENT_IP, Config.PORT);
        ManagedChannel coordinatorChannel =
                ChannelUtil.getInstance().newClientChannel(Config.COORDINATOR_IP, Config.COORDINATOR_PORT);
//        IdentifyGrpc.IdentifyBlockingStub stub = IdentifyGrpc.newBlockingStub(managementChannel);
        IdentifyGrpc.IdentifyStub stub = IdentifyGrpc.newStub(managementChannel);
        stub.identify(null, new StreamObserver<Identification>() {
            @Override
            public void onNext(Identification identification) {
                logger.info("Got identification " + identification);

                // Register to the coordinator
                ServiceRegistrationGrpc.ServiceRegistrationStub registrationStub =
                        ServiceRegistrationGrpc.newStub(coordinatorChannel);
                registrationStub.registerService(ServiceInfo.newBuilder().addAllName(services.stream()
                        .map(Service::getName).collect(Collectors.toList())).setJobId(Config.JOB_ID)
                        .setId(identification.getId()).build(), new StreamObserver<RegistrationResponse>() {
                    @Override
                    public void onNext(RegistrationResponse response) {
                        logger.info("Registration result: " + response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.error(t.getMessage(), t);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                logger.error(t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
            }
        });


        server.awaitTermination();
    }
}
