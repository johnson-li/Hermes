package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import participants.Participant;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;

public class Starter {
    static Logger logger = LoggerFactory.getLogger(Starter.class);

    public static void main(String[] args) throws Exception {
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
//        if (participant.register().getStatus() != Status.SUCCESS) {
//            throw new IllegalStateException("Fail to register");
//        }
        participant.await();
    }
}
