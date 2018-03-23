package jobs;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class JobManager {
    private static Logger logger = LoggerFactory.getLogger(JobManager.class);
    private static List<Job> jobs = new ArrayList<>();

    private static JobManager instance = new JobManager();

    private JobManager() {
        jobs.add(new EchoJob());
        try {
            ClassPath.from(getClass().getClassLoader()).getTopLevelClasses("jobs").stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(clazz -> !clazz.equals(Job.class) && Job.class.isAssignableFrom(clazz))
                    .map(this::newJob)
                    .forEach(jobs::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JobManager getInstance() {
        return instance;
    }

    private Job newJob(Class clazz) {
        try {
            Constructor constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            return (Job) object;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Job getByName(String name) {
        return jobs.stream().filter(job -> job.getJobName().equals(name)).findFirst().orElse(null);
    }
}
