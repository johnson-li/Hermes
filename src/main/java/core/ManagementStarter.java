package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import participants.Participant;
import proto.hermes.Status;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;

public class ManagementStarter {
    private static Logger logger = LoggerFactory.getLogger(ManagementStarter.class);

    public static void main(String[] args) throws Exception {
        new ManagementStarter().start();
    }

    public void start() throws Exception {
        logger.info("Starting...");
        Participant participant = new Participant(Config.HOST, Config.PORT);
        for (String str : Config.ROLES) {
            switch (str) {
                case "producer":
                    participant.addRoles(new Producer(participant));
                    break;
                case "consumer":
                    participant.addRoles(new Consumer(participant));
                    break;
                case "client":
                    participant.addRoles(new Client(participant));
                    break;
                case "coordinator":
                    participant.addRoles(new Coordinator(participant));
                    break;
            }
        }
        participant.start();

        // Register to the coordinator
        if (participant.getRoles().stream().anyMatch(role -> !(role instanceof Coordinator))) {
            if (participant.register().getStatus() != Status.SUCCESS) {
                throw new IllegalStateException("Fail to register");
            }
            participant.startHeartbeat();
        }

        // Start the requests
        participant.delegate(Client.class).ifPresent(client -> {
            client.initJob();
            client.startJob();

            try {
                Thread.sleep(Config.RUNNING_TIME);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            client.finishJob();
        });
        participant.await();
    }
}
