package services;

import proto.hermes.Task;

public interface TaskListener {
    void onTaskAssigned(Task task);

    void onStopped(Task task);

    void onStarted(Task task);
}
