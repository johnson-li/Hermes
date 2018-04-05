package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import participants.Participant;
import proto.hermes.Status;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;
import utils.ChannelUtil;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Config.COORDINATOR_IP = "127.0.0.1";
        Participant coordinator = new Participant("localhost", 5555);
        Participant producer = new Participant("localhost", 5556);
        Participant consumer = new Participant("localhost", 5560);
        Participant client = new Participant("localhost", 5562);

        coordinator.addRoles(new Coordinator(coordinator));
        producer.addRoles(new Producer(producer));
        consumer.addRoles(new Consumer(consumer));
        client.addRoles(new Client(client));

        coordinator.start();
        producer.start();
        consumer.start();
        client.start();

        if (producer.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the producers");
        }
        if (consumer.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the consumers");
        }
        if (client.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the client");
        }

        client.delegate(Client.class).get().initJob();
        client.delegate(Client.class).get().startJob();

        Thread.sleep(5000);

        client.delegate(Client.class).get().finishJob();

        Thread.sleep(3000);
        ChannelUtil.getInstance().terminate();
    }
}
