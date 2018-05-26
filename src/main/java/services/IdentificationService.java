package services;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import participants.Participant;
import proto.hermes.Empty;
import proto.hermes.Identification;
import proto.hermes.IdentifyGrpc;
import proto.hermes.Role;

import java.util.stream.Collectors;

public class IdentificationService extends IdentifyGrpc.IdentifyImplBase implements Service {
    final Participant participant;
    private Logger logger = LoggerFactory.getLogger(IdentificationService.class);

    public IdentificationService(Participant participant) {
        this.participant = participant;
    }

    @Override
    public void identify(Empty request, StreamObserver<Identification> responseObserver) {
        logger.info("Received identification request");
        responseObserver.onNext(Identification.newBuilder().setId(participant.getId())
                .addAllRoles(participant.getRoles().stream().map(role ->
                        Role.newBuilder().setRole(role.getRoleName()).build()).collect(Collectors.toList()))
                .setIp(participant.getHost()).build());
    }
}
