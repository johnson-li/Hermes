package core;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.TextFormat;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import services.Service;
import services.ServiceControlListener;
import services.ServiceController;
import services.ServiceManager;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceStarter {
    @VisibleForTesting
    public int managementPort = Config.PORT;
    @VisibleForTesting
    public int servicePort = Config.SERVICE_PORT;
    @VisibleForTesting
    public List<String> serviceStrings = new ArrayList<>(Config.SERVICES);
    private List<Service> services;
    private String host = Config.HOST;
    private String managementIP = Config.MANAGEMENT_IP;
    private Logger logger = LoggerFactory.getLogger(ServiceStarter.class);
    private ServiceManager serviceManager = new ServiceManager();
    private Server server;
    private ServiceControlListener serviceControlListener = new ServiceControlListener() {
        @Override
        public void onInit(InitServiceRequest request) {
            services.forEach(Service::init);
            try {
                if (request.getIngress() != null && !request.getIngress().getIp().isEmpty()) {
                    logger.info("Listen on: " + TextFormat.shortDebugString(request.getIngress()));
                    ManagedChannel ingressChannel = ChannelUtil.getInstance()
                            .newClientChannel(request.getIngress().getIp(), request.getIngress().getPort());
                    ChannelUtil.getInstance().execute(() -> services.forEach(service -> service.listen(ingressChannel)));
                }
            } catch (SSLException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onStart() {
            logger.info("Start services: " + services.stream().map(Service::getName).collect(Collectors.toList()));
            services.forEach(Service::start);
        }

        @Override
        public void onStop() {
            services.forEach(Service::stop);
        }
    };

    public static void main(String[] args) throws Exception {
        ServiceStarter starter = new ServiceStarter();
        starter.start();
        starter.await();
    }

    private void await() throws InterruptedException {
        logger.info("Await");
        server.awaitTermination();
    }

    public void start() throws Exception {
        logger.info("Starting...");

        // Create service
        services = serviceStrings.stream().map(service ->
                serviceManager.getService(service)).collect(Collectors.toList());
        ServiceController controller = new ServiceController(serviceControlListener);

        // Start the server
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ServerBuilder builder = ServerBuilder.forPort(servicePort).useTransportSecurity(ssc.certificate(),
                ssc.privateKey()).addService(controller);
        logger.info("Services: " + services.stream().map(Service::getName).collect(Collectors.toList()));
        services.stream().filter(service -> service.bindService() != null).forEach(builder::addService);
        server = builder.build().start();

        logger.info("Server started");
        // Get participant id
        ManagedChannel managementChannel =
                ChannelUtil.getInstance().newClientChannel(managementIP, managementPort);
        ManagedChannel coordinatorChannel =
                ChannelUtil.getInstance().newClientChannel(Config.COORDINATOR_IP, Config.COORDINATOR_PORT);
        IdentifyGrpc.IdentifyStub stub = IdentifyGrpc.newStub(managementChannel);
        stub.identify(null, new StreamObserver<Identification>() {
            @Override
            public void onNext(Identification identification) {
                logger.info("Got identification " + TextFormat.shortDebugString(identification));
                host = identification.getIp();

                // Register to the coordinator
                ServiceRegistrationGrpc.ServiceRegistrationStub registrationStub =
                        ServiceRegistrationGrpc.newStub(coordinatorChannel);
                registrationStub.registerService(ServiceInfo.newBuilder().addAllName(services.stream()
                        .map(Service::getName).collect(Collectors.toList())).setJobId(Config.JOB_ID)
                        .setAddress(NetAddress.newBuilder().setIp(host).setPort(servicePort).build())
                        .setId(identification.getId()).build(), new StreamObserver<RegistrationResponse>() {
                    @Override
                    public void onNext(RegistrationResponse response) {
                        logger.info("Registration result: " + TextFormat.shortDebugString(response));
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
    }

    @VisibleForTesting
    public <T extends Service> void initServices(Class<T>... clazzs) {
        serviceStrings.clear();
        Arrays.stream(clazzs).map(Class::getSimpleName).forEach(serviceStrings::add);
    }
}
