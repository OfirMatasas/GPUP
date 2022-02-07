package task;

import java.util.HashSet;
import java.util.Set;

public class WorkerTasksLists {
    private final Set<String> targetsList;
    private final Set<String> tasksList;

    public WorkerTasksLists() {
        this.targetsList = new HashSet<>();
        this.tasksList = new HashSet<>();
    }

    public Set<String> getAdminsList() {
        return this.targetsList;
    }

    public Set<String> getWorkersList() {
        return this.tasksList;
    }

    public void addTargetToList(String targetName) {
        this.targetsList.add(targetName); }

    public void addTaskToList(String taskName) { this.tasksList.add(taskName); }

    public void removeTaskFromList(String taskName) { this.tasksList.remove(taskName); }
}
