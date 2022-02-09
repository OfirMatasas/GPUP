package task;

import summaries.TargetSummary;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable
{
    private final SimulationParameters targetParameters;
    private final ExecutedTargetResults summary;
    private final String targetName;
    private Duration predictedTime;

    public SimulationThread(SimulationParameters targetParameters, String targetName, ExecutedTargetResults summary) {
        this.targetParameters = targetParameters;
        this.targetName = targetName;
        this.summary = summary;
        UpdateWorkingTime();
    }

    @Override public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");
        long sleepingTime = this.targetParameters.getProcessingTime().toMillis();

        //Starting the clock
        this.summary.startTheClock();

        //Going to sleep
        try {
            Thread.sleep(this.predictedTime.toMillis());
        } catch (InterruptedException e) {
            try {
                Thread.sleep(sleepingTime - Duration.between(this.summary.getTimeStarted(), Instant.now()).toMillis());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        finally
        {
            checkForSuccess();
        }
    }

    private void UpdateWorkingTime() {
        long timeLong;
        this.predictedTime = this.targetParameters.getProcessingTime();

        if(this.targetParameters.isRandom())
        {
            timeLong = (long)(Math.random() * (this.predictedTime.toMillis())) + 1;
            this.predictedTime = Duration.of(timeLong, ChronoUnit.MILLIS);
        }
    }

    private void checkForSuccess() {
        String resultStatus;
        double result = Math.random();
        if(Math.random() <= this.targetParameters.getSuccessRate())
            resultStatus = result <= this.targetParameters.getSuccessWithWarnings() ?
                    TargetSummary.ResultStatus.Warning.toString() : TargetSummary.ResultStatus.Success.toString();
        else
            resultStatus = TargetSummary.ResultStatus.Failure.toString();

        this.summary.stopTheClock();
        this.summary.setResultStatus(resultStatus);
    }
}