import core.Config;
import core.ManagementStarter;
import core.ServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;
import services.EchoService;
import services.PrintService;
import services.RandomGenerator;

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

        clientService.initServices(RandomGenerator.class);
        consumerService.initServices(EchoService.class);
        producerService.initServices(PrintService.class);
        clientService.managementPort = client.port;
        producerService.managementPort = producer.port;
        consumerService.managementPort = consumer.port;
        clientService.servicePort = 50001;
        producerService.servicePort = 50002;
        consumerService.servicePort = 50003;
        logger.info("Start services");
        clientService.start();
        producerService.start();
        consumerService.start();
    }
}
