package dtos;

import tableItems.TaskTargetCurrentInfoTableItem;

import java.util.Set;

public class TaskCurrentInfoDTO {
    private String taskStatus;
    private final Set<TaskTargetCurrentInfoTableItem> targetStatusSet;
    private Integer currentWorkers;
    private String logHistory;

    public TaskCurrentInfoDTO(String taskStatus, Set<TaskTargetCurrentInfoTableItem> targetStatusSet, Integer currentWorkers, String logHistory) {
        this.taskStatus = taskStatus;
        this.targetStatusSet = targetStatusSet;
        this.currentWorkers = currentWorkers;
        this.logHistory = logHistory;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }

    public Set<TaskTargetCurrentInfoTableItem> getTargetStatusSet() {
        return this.targetStatusSet;
    }

    public Integer getCurrentWorkers() {
        return this.currentWorkers;
    }

    public String getLogHistory() {
        return this.logHistory;
    }

    public void changeTaskStatus(String newStatus) { this.taskStatus = newStatus; }

    public void addToLogHistory(String addedInfo) { this.logHistory += addedInfo + "\n"; }

    public synchronized void workerRegisteredToTask() { ++this.currentWorkers; }

    public synchronized void workerLeftTask() { --this.currentWorkers;}
}
