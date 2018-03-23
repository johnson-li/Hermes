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
        Participant coordinator = new Participant("localhost", 5555);
        Participant producer1 = new Participant("localhost", 5556);
        Participant producer2 = new Participant("localhost", 5557);
        Participant producer3 = new Participant("localhost", 5558);
        Participant producer4 = new Participant("localhost", 5559);
        Participant consumer1 = new Participant("localhost", 5560);
        Participant consumer2 = new Participant("localhost", 5561);
        Participant client = new Participant("localhost", 5562);

        coordinator.addRoles(new Coordinator(coordinator));
        producer1.addRoles(new Producer(producer1));
        producer2.addRoles(new Producer(producer2));
        producer3.addRoles(new Producer(producer3));
        producer4.addRoles(new Producer(producer4));
        consumer1.addRoles(new Consumer(consumer1));
        consumer2.addRoles(new Consumer(consumer2));
        client.addRoles(new Client(client));

        coordinator.start();
        producer1.start();
        producer2.start();
        producer3.start();
        producer4.start();
        consumer1.start();
        consumer2.start();
        client.start();

        if (producer1.register().getStatus() != Status.SUCCESS || producer2.register().getStatus() != Status.SUCCESS
                || producer3.register().getStatus() != Status.SUCCESS || producer4.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the producers");
        }
        if (consumer1.register().getStatus() != Status.SUCCESS || consumer2.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the consumers");
        }
        if (client.register().getStatus() != Status.SUCCESS) {
            throw new IllegalStateException("Failed to register the client");
        }

        client.delegate(Client.class).get().initJob();
        client.delegate(Client.class).get().startJob();

        Thread.sleep(100000);

        client.delegate(Client.class).get().finishJob();

        Thread.sleep(3000);
        ChannelUtil.getInstance().terminate();
    }
}
