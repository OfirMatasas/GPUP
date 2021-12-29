package task;

import java.time.Duration;

public class TaskParameters {
    private static Duration processingTime;
    private Boolean isRandom;
    private Double successRate, successWithWarnings;

    public TaskParameters() {
        processingTime = null;
        this.isRandom = false;
        this.successRate = this.successWithWarnings = 0.0;
    }

    public TaskParameters(Duration processingTime, Boolean isRandom, Double successRate, Double successWithWarnings) {
        TaskParameters.processingTime = processingTime;
        this.isRandom = isRandom;
        this.successRate = successRate;
        this.successWithWarnings = successWithWarnings;
    }

    static public Duration getProcessingTime() {
        return processingTime;
    }

    public Boolean isRandom() {
        return isRandom;
    }

    public void setRandom(Boolean random) {
        isRandom = random;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getSuccessWithWarnings() {
        return successWithWarnings;
    }

    public void setSuccessWithWarnings(Double successWithWarnings) {
        this.successWithWarnings = successWithWarnings;
    }

    public void setProcessingTime(Duration processingTime) {
        TaskParameters.processingTime = processingTime;
    }
}
