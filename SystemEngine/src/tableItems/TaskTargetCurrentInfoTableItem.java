package tableItems;

public class TaskTargetCurrentInfoTableItem {
    private final Integer targetNumber;
    private final String targetName;
    private String runtimeStatus;
    private String resultStatus;

    public TaskTargetCurrentInfoTableItem(Integer targetNumber, String targetName, String runtimeStatus, String resultStatus) {
        this.targetNumber = targetNumber;
        this.targetName = targetName;
        this.runtimeStatus = runtimeStatus;
        this.resultStatus = resultStatus;
    }

    public Integer getTargetNumber() {
        return this.targetNumber;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public String getRuntimeStatus() {
        return this.runtimeStatus;
    }

    public String getResultStatus() {
        return this.resultStatus;
    }

    public void updateItem(TaskTargetCurrentInfoTableItem newInfo)
    {
        this.runtimeStatus = newInfo.getRuntimeStatus();
        this.resultStatus = newInfo.getResultStatus();
    }

    public void setRuntimeStatus(String runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
