package participants;

import core.Config;
import core.Context;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
import roles.Role;
import services.HeartbeatClient;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

public class Participant extends Context {
    private static Logger logger = LoggerFactory.getLogger(Participant.class);
    private final HeartbeatClient heartbeatClient;
    private ManagedChannel channel;
    private Set<Role> roles = new HashSet<>();
    private int port;
    private String host;
    private Server server;

    public Participant(String host, int port, long id) {
        super(id);
        this.host = host;
        this.port = port;
        heartbeatClient = new HeartbeatClient(id);
    }

    public Participant(String host, int port) {
        this(host, port, UUID.randomUUID().getLeastSignificantBits());
    }

    public void addRoles(Role... roles) {
        Arrays.stream(roles).forEach(Role::init);
        this.roles.addAll(Arrays.asList(roles));
    }

    public void start() throws IOException, CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ServerBuilder builder = ServerBuilder.forPort(port).useTransportSecurity(ssc.certificate(), ssc.privateKey());
        logger.info("Roles: " + String.join(", ", roles.stream().map(Role::getRoleName).collect(Collectors.toList())));
        for (Role role : roles) {
            role.getServices().stream().filter(service -> service.bindService() != null).forEach(service -> {
                logger.info("Add service: " + service.getClass().getSimpleName());
                builder.addService(service);
            });
        }
        server = builder.build();
        server.start();
    }

    public void await() throws InterruptedException {
        server.awaitTermination();
    }

    public Collection<Role> getRoles() {
        return roles;
    }

    public RegistrationResult register() {
        logger.info(String.format("Register to %s:%s", Config.COORDINATOR_IP, Config.COORDINATOR_PORT));
        try {
            channel = ChannelUtil.getInstance().newClientChannel(Config.COORDINATOR_IP, Config.COORDINATOR_PORT);
            roles.forEach(role -> role.setChannel(channel));
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Registration failed");
        }
        List<proto.hermes.Role> roleList = roles.stream().map(role ->
                proto.hermes.Role.newBuilder().setRole(role.getRoleName()).build()).collect(Collectors.toList());
        List<Service> serviceList = roles.stream().flatMap(role ->
                role.getServices().stream().map(services.Service::getService)).collect(Collectors.toList());
        RegistrationGrpc.RegistrationBlockingStub stub = RegistrationGrpc.newBlockingStub(channel);
        proto.hermes.Participant participant =
                proto.hermes.Participant.newBuilder().setId(id).addAllRoles(roleList).addAllServices(serviceList)
                        .setAddress(NetAddress.newBuilder().setIp(host).setPort(port).build()).build();
        RegistrationRequest request = RegistrationRequest.newBuilder().setParticipant(participant).build();
        return stub.register(request);
    }

    public void startHeartbeat() {
        try {
            channel = ChannelUtil.getInstance().newClientChannel(Config.COORDINATOR_IP, Config.COORDINATOR_PORT);
        } catch (SSLException e) {
            logger.error(e.getMessage(), e);
        }
        heartbeatClient.listen(channel);
        heartbeatClient.start();
    }

    @SuppressWarnings("unchecked")
    public <T extends Role> Optional<T> delegate(Class<T> clazz) {
        return (Optional<T>) getRoles().stream().filter(role -> clazz.isInstance(role)).findFirst();
    }
}
