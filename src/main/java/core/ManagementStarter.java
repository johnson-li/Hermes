package core;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import participants.Participant;
import proto.hermes.FinishJobResult;
import proto.hermes.InitJobResult;
import proto.hermes.StartJobResult;
import proto.hermes.Status;
import roles.Client;
import roles.Consumer;
import roles.Coordinator;
import roles.Producer;
import services.JobListener;

import java.util.ArrayList;
import java.util.List;

public class ManagementStarter {
    private static Logger logger = LoggerFactory.getLogger(ManagementStarter.class);
    @VisibleForTesting
    public String host = Config.HOST;
    @VisibleForTesting
    public int port = Config.PORT;
    @VisibleForTesting
    public List<String> roles = new ArrayList<>(Config.ROLES);
    private JobListener jobListener = new JobListener() {
        @Override
        public void onInit(Client client, InitJobResult result) {
            try {
                if (Config.PREPARATION_TIME > 0) {
                    Thread.sleep(Config.PREPARATION_TIME);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            client.startJob(this);
        }

        @Override
        public void onStart(Client client, StartJobResult result) {
            try {
                if (Config.RUNNING_TIME > 0) {
                    Thread.sleep(Config.RUNNING_TIME);
                    client.finishJob(this);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFinish(Client client, FinishJobResult result) {
        }
    };
    private Participant participant;

    public static void main(String[] args) throws Exception {
        ManagementStarter starter = new ManagementStarter();
        starter.start();
        starter.await();
    }

    public void start() throws Exception {
        logger.info("Starting...");
        participant = new Participant(host, port);
        for (String str : roles) {
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

        if (Config.AUTO_PLAY) {
            sendRequest(jobListener);
        }
    }

    public void sendRequest(JobListener listener) {
        // Start the requests
        participant.delegate(Client.class).ifPresent(client -> client.initJob(listener));
    }

    private void await() throws InterruptedException {
        participant.await();
    }
}
