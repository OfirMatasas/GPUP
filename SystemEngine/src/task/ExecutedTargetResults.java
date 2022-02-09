package task;

import java.time.Duration;
import java.time.Instant;

public class ExecutedTargetResults {
    private final String targetName;
    private String runtimeStatus;
    private String resultStatus;
    private Instant timeStarted;
    private Duration totalTimeSlept;

    public ExecutedTargetResults(String targetName) {
        this.targetName = targetName;
        this.runtimeStatus = "In process";
        this.resultStatus = "Undefined";
        this.totalTimeSlept = Duration.ZERO;
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

    public Instant getTimeStarted() {
        return this.timeStarted;
    }

    public Duration getTimeSlept() {
        return this.totalTimeSlept;
    }

    public void setRuntimeStatus(String runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public void startTheClock() {
        this.timeStarted = Instant.now();
        this.runtimeStatus = "In process";
    }

    public void stopTheClock() {
        this.totalTimeSlept = Duration.between(this.timeStarted, Instant.now());
        this.runtimeStatus = "Finished";
    }
}
