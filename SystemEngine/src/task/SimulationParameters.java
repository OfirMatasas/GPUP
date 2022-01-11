package task;

import java.time.Duration;

public class SimulationParameters {
    private Duration processingTime;
    private Boolean isRandom;
    private Double successRate, successWithWarnings;

    public SimulationParameters() {
        this.processingTime = null;
        this.isRandom = false;
        this.successRate = this.successWithWarnings = 0.0;
    }

    public SimulationParameters(Duration processingTime, Boolean isRandom, Double successRate, Double successWithWarnings) {
        this.processingTime = processingTime;
        this.isRandom = isRandom;
        this.successRate = successRate;
        this.successWithWarnings = successWithWarnings;
    }

    public Duration getProcessingTime() {
        return this.processingTime;
    }

    public Boolean isRandom() {
        return this.isRandom;
    }

    public void setRandom(Boolean random) {
        this.isRandom = random;
    }

    public Double getSuccessRate() {
        return this.successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getSuccessWithWarnings() {
        return this.successWithWarnings;
    }

    public void setSuccessWithWarnings(Double successWithWarnings) {
        this.successWithWarnings = successWithWarnings;
    }

    public void setProcessingTime(Duration processingTime) {
        this.processingTime = processingTime;
    }
}