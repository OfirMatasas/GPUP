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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskThread extends Thread {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum TaskType { Simulation, Compilation }

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
    private ArrayBlockingQueue<String> targetsQueue;
    private final ClosedSerialSets closedSerialSets;

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
        this.closedSerialSets = new ClosedSerialSets();
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        String currTargetName;
        Target currTarget;
        taskPreparations();

        //Starting task on graph
        printStartOfTaskOnGraph(graph.getGraphName());
//        taskOutput.printStartOfTaskOnGraph(graph.getGraphName());

        graphSummary.startTheClock();

        if(taskType.equals(TaskType.Simulation))
        {
            while((currTargetName = targetsQueue.poll()) != null)
            {
                currTarget = graph.getTarget(currTargetName);
                try {
                    if(graphSummary.isSkipped(currTargetName))
                        continue;
                    else if(graphSummary.isTargetReadyToRun(currTarget, targetsSet, closedSerialSets))
                    {
                        closedSerialSets.addClosedSerialSets(currTarget);
                        executor.execute(new SimulationThread(taskParametersMap.get(currTargetName), currTarget, graphSummary, taskOutput, closedSerialSets));
                    }
                    else
                        targetsQueue.add(currTargetName);
                } catch (FileNotFound | IOException | OpeningFileCrash e) {
                    String finalCurrTarget = currTargetName;
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
        outputGraphSummary(graphSummary);
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
        String outputString = "Graph task summary:\n";
        graphSummary.calculateResults();
        Map<TargetSummary.ResultStatus, Integer> results = graphSummary.getAllResultStatus();

        outputString += String.format("Total time spent on task: %02d:%02d:%02d\n",
                time.toHours(), time.toMinutes(), time.getSeconds());
        outputString += "Number of targets succeeded: " + results.get(TargetSummary.ResultStatus.Success) + "\n";
        outputString += "Number of targets succeeded with warnings: " + results.get(TargetSummary.ResultStatus.Warning) + "\n";
        outputString += "Number of targets failed: " + results.get(TargetSummary.ResultStatus.Failure) + "\n";
        outputString += "Number of targets skipped: " + graphSummary.getSkippedTargets();

        String finalOutputString = outputString;
        Platform.runLater(() ->
        {
            System.out.println(finalOutputString);
        });

        for(TargetSummary currentTarget : graphSummary.getTargetsSummaryMap().values())
        {
            if(currentTarget.isRunning())
                outputTargetTaskSummary(currentTarget);
        }
//        System.out.println("----------------------------------\n");
    }

    public void outputTargetTaskSummary(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getTime();
        String targetName, timeSpentFormatted;
        String outputString = "-----------------------\n";
        outputString += "Target's name :" + targetSummary.getTargetName() + "\n";
        outputString += "Target's result status: ";

        if(targetSummary.isSkipped())
            outputString += "Skipped\n";
        else
            outputString += targetSummary.getResultStatus() + "\n";

        if(!targetSummary.isSkipped())
            outputString += String.format("Target's running time: %02d:%02d:%02d\n", time.toHours(), time.toMinutes(), time.getSeconds());

        String finalOutputString = outputString;
        Platform.runLater(() ->
        {
            System.out.println(finalOutputString);
        });
    }

    private void taskPreparations()
    {
        TargetSummary currentTargetSummary;
        Target currentTarget;
        String dependedTargetName;
        this.targetsQueue = new ArrayBlockingQueue<String>(targetsSet.size());

        //Preparing the graph summary
        for(Target target : graph.getGraphTargets().values())
            graphSummary.getTargetsSummaryMap().get(target.getTargetName()).setRunning(false);
        graphSummary.setSkippedTargetsToZero();
        //Finished preparing the graph summary

        //Initializing graph summary for current run
        for(String currentTargetName : targetsSet)
        {
            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTargetName);
            targetsQueue.add(currentTargetName);

            currentTargetSummary.setRunning(true);
            currentTargetSummary.setResultStatus(TargetSummary.ResultStatus.Undefined);

            currentTarget = graph.getTarget(currentTargetName);
            for(Target dependedTarget : currentTarget.getDependsOnTargets())
            {
                if(targetsSet.contains(dependedTarget.getTargetName()))
                    currentTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Frozen);
                else
                    currentTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Waiting);
            }

//            currentTargetSummary.setOpenedTargetsToZero();
        }
        //Finished initializing graph summary
    }
}
