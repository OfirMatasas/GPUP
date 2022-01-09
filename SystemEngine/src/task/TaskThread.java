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
    private ExecutorService executor;
    private final TextArea log;
    private final Boolean incremental;
    private int numOfThreads;

    //Local use
    private final TaskOutput taskOutput;
    private final LinkedList<String> targetsList;
    private Boolean paused;
    private Boolean stopped;
    private Boolean pausedBefore;
    private Boolean statusChanged;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, TaskParameters> taskParametersMap,
                      GraphSummary graphSummary, Set<String> targetsSet, int numOfThreads, TextArea log, Boolean incremental) throws FileNotFoundException, OpeningFileCrash {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.graphSummary = graphSummary;
        this.targetsSet = targetsSet;
        this.numOfThreads = numOfThreads;
        this.executor = Executors.newFixedThreadPool(numOfThreads);
        this.taskOutput = new TaskOutput(taskType.toString(), graphSummary);
        this.targetsList = new LinkedList<>();
        this.log = log;
        this.paused = false;
        this.stopped = false;
        this.statusChanged = false;
        this.incremental = incremental;
        this.pausedBefore = false;
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        String currTargetName = null;
        Target currTarget;
        taskPreparations();
        Boolean currentlyPaused = false;

        if(targetsList.isEmpty())
        {
            Platform.runLater(() -> ShowPopUp("There are no targets available for the current task!", "Lack of available targets", Alert.AlertType.ERROR));
            return;
        }

        //Starting task on graph
        printStartOfTaskOnGraph(graph.getGraphName());
//        taskOutput.printStartOfTaskOnGraph(graph.getGraphName());

        graphSummary.startTheClock();

        if(taskType.equals(TaskType.Simulation))
        {
            //Continuing polling targets while there are some left, and the "Stop" button didn't hit
            while(!getStopped() && (currentlyPaused || ((currTargetName = targetsList.poll()) != null)))
            {
                //If the task is on "pause"
                if(currentlyPaused)
                {
                    //Pausing the clock if the "Pause" button just hit
                    if(!pausedBefore)
                    {
                        executor.shutdown();
                        while(!executor.isTerminated()) {}

                        pausedBefore = true;
                        graphSummary.pauseTheClock();
                    }

                    currentlyPaused = getPaused();
                    continue;
                }

                currTarget = graph.getTarget(currTargetName);

                try {
                    //Continuing the clock if the "Resume" button just hit
                    if(pausedBefore)
                    {
                        this.executor = Executors.newFixedThreadPool(numOfThreads);

                        pausedBefore = false;
                        graphSummary.continueTheClock();
                    }

                    if(graphSummary.isSkipped(currTargetName))
                        continue;
                    else if(graphSummary.isTargetReadyToRun(currTarget, targetsSet) && graphSummary.checkIfSerialSetsAreOpen(currTarget.getSerialSets()))
                    { //The target is ready to run!
                        graphSummary.addClosedSerialSets(currTarget);
                        executor.execute(new SimulationThread(taskParametersMap.get(currTargetName), currTarget, graphSummary, log));
                    }
                    else //The target is not ready to run yet, but not skipped either
                        targetsList.addLast(currTargetName);

                    currentlyPaused = getPaused();

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
        while (!executor.isTerminated()) { }

        graphSummary.stopTheClock();
        outputGraphSummary(graphSummary);
        Platform.runLater(() -> ShowPopUp("Task completed!\nCheck \"Task\" for more information.", "Task completed!", Alert.AlertType.INFORMATION));
//        taskOutput.outputGraphSummary();
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
        TargetSummary.ResultStatus resultStatus;

        //Preparing the graph summary
        for(Target target : graph.getGraphTargets().values())
        {
            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(target.getTargetName());
            resultStatus = currentTargetSummary.getResultStatus();
            currentTargetSummary.setRunning(targetsSet.contains(target.getTargetName())
                    && (!incremental || resultStatus.equals(TargetSummary.ResultStatus.Failure)));
            currentTargetSummary.setSkipped(false);
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
            resultStatus = currentTargetSummary.getResultStatus();

            //Skipping the targets who finished successfully on the last run
            if(incremental && !resultStatus.equals(TargetSummary.ResultStatus.Failure) && !currentTargetSummary.isSkipped())
                continue;

            currentTargetSummary.setResultStatus(TargetSummary.ResultStatus.Undefined);

            for(String dependedTarget : currentTarget.getAllDependsOnTargets())
            {
                //Check if the current target has depends-on-targets in the current run (frozen)
                if(targetsSet.contains(dependedTarget))
                {
                    //Checking if the depended target will run in the current task (when incrementing)
                    resultStatus = graphSummary.getTargetsSummaryMap().get(dependedTarget).getResultStatus();
                    if(incremental && (resultStatus.equals(TargetSummary.ResultStatus.Success)
                            || resultStatus.equals(TargetSummary.ResultStatus.Warning)))
                    {
                        continue;
                    }
                    targetFrozen = true;
                    targetsList.addLast(currentTargetName);
                    graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Frozen, true);

                    break;
                }
            }
            if(!targetFrozen)
            {
                targetsList.addFirst(currentTargetName);
                graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting, true);
            }

//            currentTargetSummary.setOpenedTargetsToZero();
        }
        //Finished initializing graph summary

        //Clearing the log text area
        log.clear();
        log.setDisable(false);
    }

    public void setNumOfThreads(int numOfThreads) { this.numOfThreads = numOfThreads; }

    public Boolean getPaused() { return this.paused; }

    public Boolean getStopped() { return this.stopped; }

    public Boolean getStatusChanged() { return this.statusChanged; }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void stopTheTask()
    {
        this.stopped = this.statusChanged = true;
        this.executor.shutdown();
    }

    public void pauseTheTask() { this.paused = this.statusChanged = true; }

    public void continueTheTask() {
        this.paused = false;
        this.statusChanged = true;
    }

    public void resetStatusChanged() { this.statusChanged = false; }
}