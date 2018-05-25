package services;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import participants.Participant;
import proto.hermes.Empty;
import proto.hermes.Identification;
import proto.hermes.IdentifyGrpc;
import proto.hermes.Role;

import java.util.stream.Collectors;

public class IdentificationService extends IdentifyGrpc.IdentifyImplBase implements Service {
    final Participant participant;

    public IdentificationService(Participant participant) {
        this.participant = participant;
    }

    @Override
    public void listen(ManagedChannel channel) {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void identify(Empty request, StreamObserver<Identification> responseObserver) {
        responseObserver.onNext(Identification.newBuilder().setId(participant.getId())
                .addAllRoles(participant.getRoles().stream().map(role ->
                        Role.newBuilder().setRole(role.getRoleName()).build()).collect(Collectors.toList()))
                .setIp(participant.getHost()).build());
    }
}
