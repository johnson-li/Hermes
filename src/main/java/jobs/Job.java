package jobs;

import proto.hermes.Task;

import java.util.List;
import java.util.UUID;

public abstract class Job {
    private final long ID = UUID.randomUUID().getMostSignificantBits();

    public static <T extends Job> String getJobName(Class<T> clazz) {
        return clazz.getSimpleName();
    }

    public abstract List<Task> getTasks();

    public abstract void setTasks(List<Task> tasks);

    public String getJobName() {
        return getClass().getSimpleName();
    }

    public long getID() {
        return ID;
    }
}
