import core.Config;
import core.ManagementStarter;
import core.ServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.FinishJobResult;
import proto.hermes.InitJobResult;
import proto.hermes.StartJobResult;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;
import services.*;

public class IntegrationTest {
    static {
        Config.COORDINATOR_IP = "127.0.0.1";
        Config.MANAGEMENT_IP = "127.0.0.1";
        Config.HOST = "127.0.0.1";
        Config.COORDINATOR_PORT = 40000;
        Config.RUNNING_TIME = -1;
        Config.PREPARATION_TIME = -1;
        Config.SERVICES.add(EchoService.class.getSimpleName());
        Config.JOB_ID = 12345;
        Config.AUTO_PLAY = false;
//        WebrtcSenderService.CLIENT_PATH = "/home/lix16/Workspace/webrtc/src/out/Default/peerconnection_client_terminal";
//        WebrtcServer.SERVER_PATH = "/home/lix16/Workspace/webrtc/src/out/Default/peerconnection_server";
    }

    private Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

    public static void main(String[] args) throws Exception {
        new IntegrationTest().start();
    }

    private void start() throws Exception {
        logger.info("Start managements");
        ManagementStarter client = new ManagementStarter();
        ManagementStarter producer = new ManagementStarter();
        ManagementStarter consumer = new ManagementStarter();
        ManagementStarter coordinator = new ManagementStarter();

        client.roles.add(Client.class.getSimpleName().toLowerCase());
        producer.roles.add(Producer.class.getSimpleName().toLowerCase());
        consumer.roles.add(Consumer.class.getSimpleName().toLowerCase());
        coordinator.roles.add(Coordinator.class.getSimpleName().toLowerCase());

        coordinator.port = 40000;
        client.port = 40001;
        producer.port = 40002;
        consumer.port = 40003;

        coordinator.start();
        Thread.sleep(1000);
        producer.start();
        consumer.start();
        client.start();

        Thread.sleep(1000);
        logger.info("Init services");
        ServiceStarter clientService = new ServiceStarter();
        ServiceStarter producerService = new ServiceStarter();
        ServiceStarter consumerService = new ServiceStarter();

        clientService.initServices(WebrtcClientService.class);
        consumerService.initServices(WebrtcReceiverService.class);
        producerService.initServices(WebrtcSenderService.class);
        clientService.managementPort = client.port;
        producerService.managementPort = producer.port;
        consumerService.managementPort = consumer.port;
        clientService.servicePort = 50001;
        producerService.servicePort = 50002;
        consumerService.servicePort = 50003;

        logger.info("Ready to send requests");
        client.sendRequest(new JobListener() {
            @Override
            public void onInit(Client client, InitJobResult result) {
                client.startJob(this);
            }

            @Override
            public void onStart(Client client, StartJobResult result) {
                logger.info("started");
            }

            @Override
            public void onFinish(Client client, FinishJobResult result) {
            }
        });

        Thread.sleep(3000);
        logger.info("Start service containers");
        clientService.start();
        producerService.start();
        consumerService.start();
    }
}
