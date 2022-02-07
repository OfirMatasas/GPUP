package tableItems;

public class WorkerChosenTargetInformationTableItem {
    private final String targetName;
    private final String taskName;
    private final String taskType;
    private final String status;
    private final Integer earnedCredits;

    public WorkerChosenTargetInformationTableItem(String targetName, String taskName, String taskType, String status, Integer earnedCredits) {
        this.targetName = targetName;
        this.taskName = taskName;
        this.taskType = taskType;
        this.status = status;
        this.earnedCredits = earnedCredits;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getTaskType() {
        return this.taskType;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getEarnedCredits() {
        return this.earnedCredits;
    }
}