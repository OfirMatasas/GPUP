package task;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class TaskThread extends Thread {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public static enum TaskType { Simulation, Compilation }

    //-------------------------------------------------Members------------------------------------------------------//
    //Getting from UI
    private final Graph graph;
    private final TaskType taskType;
    private final Map<String, TaskParameters> taskParametersMap;
    private final GraphSummary graphSummary;
    private final Set<String> targetsSet;

    //Local use
    private final ExecutorService executor;
    private final TaskOutput taskOutput;
    private BlockingQueue<String> targetsQueue;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, TaskParameters> taskParametersMap,
                      GraphSummary graphSummary, Set<String> targetsSet, int threadsNum) throws FileNotFoundException, OpeningFileCrash {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.graphSummary = graphSummary;
        this.targetsSet = targetsSet;
        this.executor = Executors.newFixedThreadPool(Math.min(threadsNum, 10));
        this.taskOutput = new TaskOutput(taskType.toString(), graphSummary);
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        String currTarget;
        taskPreparations();

        //Starting task on graph
        printStartOfTaskOnGraph(graph.getGraphName());
//        taskOutput.printStartOfTaskOnGraph(graph.getGraphName());

        graphSummary.startTheClock();

        if(taskType.equals(TaskType.Simulation))
        {
            while((currTarget = targetsQueue.poll()) != null)
            {
                try {
                    if(graphSummary.isTargetReadyToRun(graph.getTarget(currTarget), targetsSet))
                        executor.execute(new SimulationThread(taskParametersMap.get(currTarget), graph, graphSummary, currTarget, taskOutput));
                    else if(graphSummary.isSkipped(currTarget))
                        continue;
                    else
                        targetsQueue.add(currTarget);
                } catch (FileNotFound | IOException | OpeningFileCrash e) {
                    String finalCurrTarget = currTarget;
                    Platform.runLater(() -> ErrorPopup(e, "Error with " + finalCurrTarget + " file."));
                }
            }
        }
        else if(taskType.equals(TaskType.Compilation))
        {
            System.out.println("Compilation task");
        }

        executor.shutdown();
        while (!executor.isTerminated()) {   }

        graphSummary.stopTheClock();
        Platform.runLater(() -> outputGraphSummary(graphSummary));
//        taskOutput.outputGraphSummary();
    }

    public void printStartOfTaskOnGraph(String graphName) {
        System.out.println("Task started on graph " + graphName + "!");
    }

    private void ErrorPopup(Exception ex, String title)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    public void outputGraphSummary(GraphSummary graphSummary)
    {
        Duration time = graphSummary.getTime();
        System.out.println("Graph task summary:\n");

        String timeSpentFormatted = String.format("Total time spent on task: %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());
        System.out.println(timeSpentFormatted);

        graphSummary.calculateResults();
        Map<TargetSummary.ResultStatus, Integer> results = graphSummary.getAllResultStatus();
        String succeeded, warnings, failed, skipped;

        succeeded = "Number of targets succeeded: " + results.get(TargetSummary.ResultStatus.Success) + "\n";
        System.out.println(succeeded);

        warnings = "Number of targets succeeded with warnings: " + results.get(TargetSummary.ResultStatus.Warning) + "\n";
        System.out.println(warnings);

        failed = "Number of targets failed: " + results.get(TargetSummary.ResultStatus.Failure) + "\n";
        System.out.println(failed);

        skipped = "Number of targets skipped: " + graphSummary.getSkippedTargets() + "\n";
        System.out.println(skipped);

        for(TargetSummary currentTarget : graphSummary.getTargetsSummaryMap().values())
        {
            if(currentTarget.isRunning())
                outputTargetTaskSummary(currentTarget);
        }
        System.out.println("----------------------------------\n");
    }

    public void outputTargetTaskSummary(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getTime();
        System.out.println("-----------------------\n");

        String targetName, timeSpentFormatted;
        String result = "Target's result status: ";

        targetName = "Target's name :" + targetSummary.getTargetName() + "\n";
        System.out.println(targetName);

        if(targetSummary.isSkipped())
            result += "Skipped\n";
        else
            result += targetSummary.getResultStatus() + "\n";
        System.out.println(result);

        if(!targetSummary.isSkipped())
        {
            timeSpentFormatted = String.format("Target's running time: %02d:%02d:%02d\n", time.toHours(), time.toMinutes(), time.getSeconds());
            System.out.println(timeSpentFormatted);
        }
    }

    private void taskPreparations()
    {
        TargetSummary currentTargetSummary;
        this.targetsQueue = new ArrayBlockingQueue<String>(targetsSet.size());

        //Resetting the graph summary
        for(Target currentTarget : graph.getGraphTargets().values())
        {
            graphSummary.getTargetsSummaryMap().get(currentTarget.getTargetName()).setRunning(false);
        }
        graphSummary.setSkippedTargetsToZero();

        //Initializing graph summary for current run
        for(String currentTarget : targetsSet)
        {
            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTarget);
            targetsQueue.add(currentTarget);

            currentTargetSummary.setRunning(true);
//            currentTargetSummary.setOpenedTargetsToZero();
        }
    }
}
