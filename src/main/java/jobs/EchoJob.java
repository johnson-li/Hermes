package jobs;

import proto.hermes.Participant;
import proto.hermes.Role;
import proto.hermes.Service;
import proto.hermes.Task;
import roles.Client;
import roles.Consumer;
import roles.Producer;
import services.EchoService;
import services.PrintService;

import java.util.ArrayList;
import java.util.List;

public class EchoJob extends Job {
    private List<Task> tasks = new ArrayList<>();

    @Override
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public List<Task> getTasks() {
        if (tasks.isEmpty()) {
            // for producer
            tasks.add(Task.newBuilder()
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .build());
            tasks.add(Task.newBuilder()
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .build());
            tasks.add(Task.newBuilder()
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .build());
            tasks.add(Task.newBuilder()
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .build());
            // for consumer
            tasks.add(Task.newBuilder()
                    .setService(Service.newBuilder().setName(EchoService.class.getSimpleName()).build())
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Client.class.getSimpleName()).build()).build())
                    .build());
            tasks.add(Task.newBuilder()
                    .setService(Service.newBuilder().setName(EchoService.class.getSimpleName()).build())
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName()).build()).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Producer.class.getSimpleName()).build()).build())
                    .setEgress(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Client.class.getSimpleName()).build()).build())
                    .build());
            // for client
            tasks.add(Task.newBuilder()
                    .setService(Service.newBuilder().setName(PrintService.class.getSimpleName()).build())
                    .setSelf(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Client.class.getSimpleName()).build()).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName())).build())
                    .addIngresses(Participant.newBuilder().addRoles(Role.newBuilder().setRole(Consumer.class.getSimpleName())).build())
                    .build());
        }
        return tasks;
    }
}
