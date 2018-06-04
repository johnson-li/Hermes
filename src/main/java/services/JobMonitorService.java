package services;

import com.google.protobuf.TextFormat;
import io.grpc.stub.StreamObserver;
import participants.Participant;
import proto.hermes.Empty;
import proto.hermes.FinishJobResult;
import proto.hermes.Job;
import proto.hermes.JobMonitorGrpc;
import roles.Client;

public class JobMonitorService extends JobMonitorGrpc.JobMonitorImplBase implements Service, JobListener {
    private final Participant participant;

    public JobMonitorService(Participant participant) {
        this.participant = participant;
    }

    @Override
    public void onFinish(Client client, FinishJobResult result) {
        logger.info(TextFormat.shortDebugString(result));
    }

    @Override
    public void finish(Job request, StreamObserver<Empty> responseObserver) {
        logger.info("Finish job: " + TextFormat.shortDebugString(request));
        participant.delegate(Client.class).ifPresent(client -> client.finishJob(this));
    }
}
