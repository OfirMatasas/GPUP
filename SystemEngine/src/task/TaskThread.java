package task;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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
    private final ExecutorService executor;
    private final TextArea log;

    //Local use
    private final TaskOutput taskOutput;
    private final LinkedList<String> targetsList;
    private Boolean paused;
    private Boolean stopped;
    private Runnable inEnd;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, TaskParameters> taskParametersMap,
                      GraphSummary graphSummary, Set<String> targetsSet, ExecutorService executor, TextArea log , Runnable inEnd) throws FileNotFoundException, OpeningFileCrash {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.graphSummary = graphSummary;
        this.targetsSet = targetsSet;
        this.executor = executor;
        this.taskOutput = new TaskOutput(taskType.toString(), graphSummary);
        this.targetsList = new LinkedList<>();
        this.log = log;
        this.paused = false;
        this.stopped = false;
        this.inEnd = inEnd;
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
            while((currTargetName = targetsList.poll()) != null)
            {
                currTarget = graph.getTarget(currTargetName);

                try {
                    if(graphSummary.isSkipped(currTargetName))
                        continue;
                    else if(graphSummary.isTargetReadyToRun(currTarget, targetsSet) && graphSummary.checkIfSerialSetsAreOpen(currTarget.getSerialSets()))
                    {
                        graphSummary.addClosedSerialSets(currTarget);

                        if(!paused && !stopped)
                            executor.execute(new SimulationThread(taskParametersMap.get(currTargetName), currTarget, graphSummary, log));
                        else
                            targetsList.addFirst(currTargetName);
                    }
                    else
                        targetsList.addLast(currTargetName);
                } catch (FileNotFound | IOException | OpeningFileCrash e) {
                    String finalCurrTarget = currTargetName;
                    Platform.runLater(() -> ShowPopUp(e.getMessage(), "Error with " + finalCurrTarget + " file.", Alert.AlertType.ERROR));
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
        Platform.runLater(() -> ShowPopUp("Task completed!\nCheck \"Task\" for more information.", "Task completed!", Alert.AlertType.INFORMATION));
//        taskOutput.outputGraphSummary();
        this.inEnd.run();
    }

    public void printStartOfTaskOnGraph(String graphName) {
        String startingAnnouncement = taskType.toString().substring(0, 1).toUpperCase() + taskType.toString().substring(1) + " task started on graph " + graphName + "!\n\n";
        Platform.runLater(() -> System.out.print(startingAnnouncement));
        Platform.runLater(() -> log.appendText(startingAnnouncement));
    }

    private void ShowPopUp(String message, String title, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
        outputString += "Number of targets skipped: " + graphSummary.getSkippedTargets() + "\n";

        for(TargetSummary currentTarget : graphSummary.getTargetsSummaryMap().values())
        {
            if(currentTarget.isRunning())
                outputString += outputTargetTaskSummary(currentTarget);
        }

        outputString += "---------------------END OF TASK---------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.print(finalOutputString));
        Platform.runLater(() -> log.appendText(finalOutputString));
    }

    public String outputTargetTaskSummary(TargetSummary targetSummary)
    {
        Duration time = targetSummary.getTime();
        String outputString = "-----------------------\n";
        outputString += "Target's name :" + targetSummary.getTargetName() + "\n";
        outputString += "Target's result status: ";

        if(targetSummary.isSkipped())
            outputString += "Skipped\n";
        else
            outputString += targetSummary.getResultStatus() + "\n";

        if(!targetSummary.isSkipped())
            outputString += String.format("Target's running time: %02d:%02d:%02d\n", time.toHours(), time.toMinutes(), time.getSeconds()) + "\n";

        return outputString;
    }

    private void taskPreparations()
    {
        TargetSummary currentTargetSummary;
        Target currentTarget;
        Boolean targetFrozen;

        //Preparing the graph summary
        for(Target target : graph.getGraphTargets().values())
        {
            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(target.getTargetName());
            currentTargetSummary.setRunning(targetsSet.contains(target.getTargetName()));
        }
        graphSummary.MakeNewClosedSerialSets();
        graphSummary.setSkippedTargetsToZero();
        //Finished preparing the graph summary

        //Initializing graph summary for current run
        for(String currentTargetName : targetsSet)
        {
            targetFrozen = false;
            currentTarget = graph.getTarget(currentTargetName);
            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTargetName);
            currentTargetSummary.setResultStatus(TargetSummary.ResultStatus.Undefined);

            for(Target dependedTarget : currentTarget.getDependsOnTargets())
            {
                //Check if the current target has depends-on-targets in the current run (frozen)
                if(targetsSet.contains(dependedTarget.getTargetName()))
                {
                    targetFrozen = true;
                    targetsList.addLast(currentTargetName);
                    currentTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Frozen);
                    break;
                }
            }
            if(!targetFrozen)
            {
                targetsList.addFirst(currentTargetName);
                currentTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Waiting);
            }

//            currentTargetSummary.setOpenedTargetsToZero();
        }
        //Finished initializing graph summary

        //Clearing the log text area
        log.clear();
        log.setDisable(false);
    }

    public Boolean getPaused() {
        return this.paused;
    }

    public Boolean getStopped() {
        return this.stopped;
    }

    public void stopTheTask()
    {
        this.stopped = true;
    }

    public void pauseTheTask() { this.paused = true; }
}
