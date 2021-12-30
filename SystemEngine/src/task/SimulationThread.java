package task;

import javafx.application.Platform;
import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class SimulationThread extends TaskKind {
    private TaskParameters targetParameters;
    private final Graph graph;
    private final GraphSummary graphSummary;
    private final TaskOutput taskOutput;
    private Path filePath;
    private final String targetName;

    public SimulationThread(TaskParameters targetParameters, Graph graph, GraphSummary graphSummary,
                            String targetName, TaskOutput taskOutput) throws FileNotFound, IOException, OpeningFileCrash {
        this.targetParameters = targetParameters;
        this.graph = graph;
        this.graphSummary = graphSummary;
        this.targetName = targetName;
        this.taskOutput = taskOutput;
        this.filePath = Paths.get(taskOutput.getDirectoryPath() + "/" + targetName + ".log");

        UpdateWorkingTime();
    }

    @Override
    public void run() {
        Target target = graph.getTarget(targetName);
        TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
        long sleepingTime = targetSummary.getPredictedTime().toMillis();
        TargetSummary.ResultStatus resultStatus;

        //Starting the clock

        targetSummary.startTheClock();

        Platform.runLater(() ->
        {
            outputStartingTaskOnTarget(targetSummary);
        });
        graphSummary.UpdateTargetSummary(target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting);

        //Going to sleep
        try {
            Thread.sleep(sleepingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Double result = Math.random();

        if(result <= targetParameters.getSuccessRate()) //Task succeeded
        {
            if(result <= targetParameters.getSuccessWithWarnings())
                resultStatus = TargetSummary.ResultStatus.Warning;
            else
                resultStatus = TargetSummary.ResultStatus.Success;
        }
        else //Task failed
            resultStatus = TargetSummary.ResultStatus.Failure;

        targetSummary.stopTheClock();
        graphSummary.UpdateTargetSummary(target, resultStatus, TargetSummary.RuntimeStatus.Finished);

        Platform.runLater(() ->
        {
            outputEndingTaskOnTarget(targetSummary);
        });
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

        String targetName, targetExtraInfo, totalTimeFormatted;

        targetName = "Task on target " + targetSummary.getTargetName() + " just started.\r\n";
        System.out.println(targetName);

        if(targetSummary.getExtraInformation() != null)
        {
            targetExtraInfo = "Target's extra information: " + targetSummary.getExtraInformation() +"\n";
            System.out.println(targetExtraInfo);
        }

        totalTimeFormatted = String.format("The system is going to sleep for %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());
        System.out.println(totalTimeFormatted);
    }

    public void outputEndingTaskOnTarget(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getTime();
        String targetName, totalTimeFormatted, result;

        targetName = "Task on target " + targetSummary.getTargetName() + " ended.\n";
        System.out.println(targetName);

        totalTimeFormatted = String.format("The system went to sleep for %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());
        System.out.println(totalTimeFormatted);

        result = "The result: " + targetSummary.getResultStatus().toString() + ".\n";
        System.out.println(result);

        System.out.println("------------------------------------------\n");
    }


}
