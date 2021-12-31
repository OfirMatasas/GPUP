package task;

import javafx.application.Platform;
import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Target;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SimulationThread implements Runnable {
    private final TaskParameters targetParameters;
    private final GraphSummary graphSummary;
//    private final TaskOutput taskOutput;
//    private Path filePath;
    private final Target target;
    private final String targetName;
    private final ClosedSerialSets closedSerialSets;

    public SimulationThread(TaskParameters targetParameters, Target target, GraphSummary graphSummary,
                            TaskOutput taskOutput, ClosedSerialSets closedSerialSets) throws FileNotFound, IOException, OpeningFileCrash {
        this.targetParameters = targetParameters;
        this.graphSummary = graphSummary;
        this.target = target;
        this.targetName = target.getTargetName();
//        this.taskOutput = taskOutput;
//        this.filePath = Paths.get(taskOutput.getDirectoryPath() + "/" + targetName + ".log");
        this.closedSerialSets = closedSerialSets;
        UpdateWorkingTime();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(targetName + " Thread");
        TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
        long sleepingTime = targetSummary.getPredictedTime().toMillis();
        TargetSummary.ResultStatus resultStatus;

        //Starting the clock
        targetSummary.startTheClock();
        outputStartingTaskOnTarget(targetSummary);
        graphSummary.UpdateTargetSummary(target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting);

        //Going to sleep
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double result = Math.random();
        if(Math.random() <= targetParameters.getSuccessRate())
            resultStatus = result <= targetParameters.getSuccessWithWarnings() ? resultStatus = TargetSummary.ResultStatus.Warning : TargetSummary.ResultStatus.Success;
        else
            resultStatus = TargetSummary.ResultStatus.Failure;

        targetSummary.stopTheClock();
        graphSummary.UpdateTargetSummary(target, resultStatus, TargetSummary.RuntimeStatus.Finished);
        outputEndingTaskOnTarget(targetSummary);
        closedSerialSets.removeClosedSerialSets(target);
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

    public void outputStartingTaskOnTarget(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getPredictedTime();
        String outputString = "Task on target " + targetSummary.getTargetName() + " just started!\n";

        if(targetSummary.getExtraInformation() != null)
            outputString += "Target's extra information: " + targetSummary.getExtraInformation() +"\n";

        outputString += String.format("The system is going to sleep for %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.println(finalOutputString));
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
    }
}
