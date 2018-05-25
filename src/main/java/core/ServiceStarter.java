package core;

import io.grpc.ManagedChannel;
import io.grpc.ServerBuilder;
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

        // Get participant id
        ManagedChannel managementChannel =
                ChannelUtil.getInstance().newClientChannel(Config.MANAGEMENT_IP, Config.PORT);
        IdentifyGrpc.IdentifyBlockingStub stub = IdentifyGrpc.newBlockingStub(managementChannel);
        Identification identification = stub.identify(Empty.newBuilder().build());

        // Create service
        List<Service> services = Config.SERVICES.stream().map(service ->
                serviceManager.getService(service.toLowerCase())).collect(Collectors.toList());
        ServiceController controller = new ServiceController();

        // Register to the coordinator
        ManagedChannel coordinatorChannel =
                ChannelUtil.getInstance().newClientChannel(Config.COORDINATOR_IP, Config.COORDINATOR_PORT);
        ServiceRegistrationGrpc.ServiceRegistrationBlockingStub registrationStub =
                ServiceRegistrationGrpc.newBlockingStub(coordinatorChannel);
        registrationStub.register(ServiceInfo.newBuilder().addAllName(services.stream().map(Service::getName)
                .collect(Collectors.toList())).setJobId(Config.JOB_ID).setId(identification.getId()).build());

        // Start the server
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ServerBuilder builder = ServerBuilder.forPort(Config.SERVICE_PORT).useTransportSecurity(ssc.certificate(),
                ssc.privateKey()).addService(controller);
        services.forEach(builder::addService);
        builder.build().start().awaitTermination();
    }
}
