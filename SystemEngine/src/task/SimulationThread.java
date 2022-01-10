package task;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Target;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable
{
    private final SimulationParameters targetParameters;
    private final GraphSummary graphSummary;
    private final TextArea log;
    private final Target target;
    private final String targetName;

    public SimulationThread(SimulationParameters targetParameters, Target target, GraphSummary graphSummary,
                            TextArea log) throws FileNotFound, IOException, OpeningFileCrash {
        this.targetParameters = targetParameters;
        this.graphSummary = graphSummary;
        this.target = target;
        this.targetName = target.getTargetName();
        this.log = log;
        UpdateWorkingTime();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(targetName + " Thread");
        TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
        long sleepingTime = targetParameters.getProcessingTime().toMillis();
        TargetSummary.ResultStatus resultStatus;

        //Starting the clock
        targetSummary.startTheClock();
        outputStartingTaskOnTarget(targetSummary, log);
        graphSummary.UpdateTargetSummary(target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.InProcess, true);

        //Going to sleep
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double result = Math.random();
        if(Math.random() <= targetParameters.getSuccessRate())
            resultStatus = result <= targetParameters.getSuccessWithWarnings() ? TargetSummary.ResultStatus.Warning : TargetSummary.ResultStatus.Success;
        else
            resultStatus = TargetSummary.ResultStatus.Failure;

        targetSummary.stopTheClock();
        graphSummary.UpdateTargetSummary(target, resultStatus, TargetSummary.RuntimeStatus.Finished, false);
        outputEndingTaskOnTarget(targetSummary);
    }

    private void UpdateWorkingTime() {
        long timeLong;
        Duration timeDuration;
        TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);

        if(targetParameters.isRandom())
        {
            timeDuration = targetParameters.getProcessingTime();
            timeLong = (long)(Math.random() * (timeDuration.toMillis())) + 1;
            timeDuration = Duration.of(timeLong, ChronoUnit.MILLIS);
            targetSummary.setPredictedTime(timeDuration);
        }
    }

    public void outputStartingTaskOnTarget(TargetSummary targetSummary, TextArea log)
    {
        Duration time = targetParameters.getProcessingTime();
        String outputString = "Task on target " + targetSummary.getTargetName() + " just started!\n";

        if(targetSummary.getExtraInformation() != null)
            outputString += "Target's extra information: " + targetSummary.getExtraInformation() +"\n";

        outputString += String.format("The system is going to sleep for %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());

        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.println(finalOutputString));
        Platform.runLater(() -> log.appendText(finalOutputString));

    }

    public void outputEndingTaskOnTarget(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getTime();
        String outputString = "Task on target " + targetSummary.getTargetName() + " ended!\n";

        outputString += "The result: " + targetSummary.getResultStatus().toString() + ".\n";
        outputString += String.format("The system went to sleep for %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());
        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.println(finalOutputString));
        Platform.runLater(() -> log.appendText(finalOutputString));
    }
}
