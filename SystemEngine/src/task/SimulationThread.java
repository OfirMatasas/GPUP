package task;

import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Target;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable
{
    private final SimulationParameters targetParameters;
    private final GraphSummary graphSummary;
    private final TaskOutput taskOutput;
    private final Target target;
    private final String targetName;

    public SimulationThread(SimulationParameters targetParameters, Target target, GraphSummary graphSummary,
                            TaskOutput taskOutput) throws FileNotFound, IOException, OpeningFileCrash {
        this.targetParameters = targetParameters;
        this.graphSummary = graphSummary;
        this.target = target;
        this.targetName = target.getTargetName();
        this.taskOutput = taskOutput;
        UpdateWorkingTime();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(this.targetName);
        long sleepingTime = this.targetParameters.getProcessingTime().toMillis();
        TargetSummary.ResultStatus resultStatus;

        //Starting the clock
        targetSummary.startTheClock();
        this.taskOutput.outputStartingSimulationTaskOnTarget(this.targetName, this.targetParameters);
        this.graphSummary.UpdateTargetSummary(this.target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.InProcess);

        //Going to sleep
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            try {
                Thread.sleep(sleepingTime - Duration.between(targetSummary.getTimeStarted(), Instant.now()).toMillis());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        finally
        {
            checkForSuccess(targetSummary);
        }
    }

    private void checkForSuccess(TargetSummary targetSummary) {
        TargetSummary.ResultStatus resultStatus;
        double result = Math.random();
        if(Math.random() <= this.targetParameters.getSuccessRate())
            resultStatus = result <= this.targetParameters.getSuccessWithWarnings() ? TargetSummary.ResultStatus.Warning : TargetSummary.ResultStatus.Success;
        else
            resultStatus = TargetSummary.ResultStatus.Failure;

        targetSummary.stopTheClock();
        this.graphSummary.UpdateTargetSummary(this.target, resultStatus, TargetSummary.RuntimeStatus.Finished);
        this.taskOutput.outputEndingSimulationTaskOnTarget(this.targetName);
    }


    private void UpdateWorkingTime() {
        long timeLong;
        Duration timeDuration;
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(this.targetName);

        if(this.targetParameters.isRandom())
        {
            timeDuration = this.targetParameters.getProcessingTime();
            timeLong = (long)(Math.random() * (timeDuration.toMillis())) + 1;
            timeDuration = Duration.of(timeLong, ChronoUnit.MILLIS);
            targetSummary.setPredictedTime(timeDuration);
        }
    }
}
