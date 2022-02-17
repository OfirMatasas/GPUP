package task;

import summaries.TargetSummary;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable
{
    private final SimulationParameters targetParameters;
    private final ExecutedTargetUpdates updates;
    private final String targetName;
    private final String workerName;
    private final String taskName;
    private Duration predictedTime;

    public SimulationThread(WorkerSimulationParameters taskInfo) {
        this.targetParameters = taskInfo.getParameters();
        this.targetName = taskInfo.getTargetName();
        this.workerName = taskInfo.getWorkerName();
        this.taskName = taskInfo.getTaskName();
        this.updates = new ExecutedTargetUpdates(this.taskName, this.targetName, this.workerName);
        UpdateWorkingTime();
    }

    @Override public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");
        Instant timeStarted;

        //Starting the clock
        this.updates.taskStarted();
        timeStarted = Instant.now();

        this.updates.goingToSleep(this.predictedTime.toMillis());
        //Going to sleep
        try {
            Thread.sleep(this.predictedTime.toMillis());
        } catch (InterruptedException e) {
            try {
                Thread.sleep(this.predictedTime.toMillis() - Duration.between(timeStarted, Instant.now()).toMillis());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        finally
        {
            checkForSuccess(timeStarted);
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

    private void checkForSuccess(Instant timeStarted) {
        String resultStatus, log;
        double result = Math.random();
        if(Math.random() <= this.targetParameters.getSuccessRate())
            resultStatus = result <= this.targetParameters.getSuccessWithWarnings() ?
                    TargetSummary.ResultStatus.Warning.toString() : TargetSummary.ResultStatus.Success.toString();
        else
            resultStatus = TargetSummary.ResultStatus.Failure.toString();

        Duration sleepingTime = Duration.between(timeStarted, Instant.now());

        log = "The worker " + this.workerName + " just finished working on target " + this.targetName + "!\n";
        log += "The result: " + resultStatus + ", total time: " + sleepingTime.toMillis() + "m/s";

        this.updates.setWorkingTime(sleepingTime.toMillis());
        this.updates.taskFinished(resultStatus, log);
    }
}