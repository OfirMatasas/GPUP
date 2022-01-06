package information;

public class TaskTargetInformation
{
    private int number;
    private String targetName;
    private String position;
    private String currentRuntimeStatus;

    public TaskTargetInformation(int number, String targetName, String position, String currentRuntimeStatus) {
        this.number = number;
        this.targetName = targetName;
        this.position = position;
        this.currentRuntimeStatus = currentRuntimeStatus;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCurrentRuntimeStatus() {
        return currentRuntimeStatus;
    }

    public void setCurrentRuntimeStatus(String currentRuntimeStatus) {
        this.currentRuntimeStatus = currentRuntimeStatus;
    }
}
