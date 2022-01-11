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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskThread extends Thread {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum TaskType { Simulation, Compilation }

    //-------------------------------------------------Members------------------------------------------------------//
    //Getting from UI
    private final Graph graph;
    private final TaskType taskType;
    private final Map<String, SimulationParameters> taskParametersMap;
    private final CompilationParameters compilationParameters;
    private final GraphSummary graphSummary;
    private final Set<String> targetsSet;
    private ExecutorService executor;
    private final TaskOutput taskOutput;
    private final Boolean incremental;
    private int numOfThreads;

    //Local use
    private final LinkedList<String> targetsList;
    private Boolean paused;
    private Boolean stopped;
    private Boolean pausedBefore;
    private Boolean statusChanged;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, SimulationParameters> taskParametersMap, CompilationParameters compilationParameters,
                      GraphSummary graphSummary, Set<String> targetsSet, int numOfThreads, TaskOutput taskOutput, Boolean incremental) throws FileNotFoundException, OpeningFileCrash {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.compilationParameters = compilationParameters;
        this.graphSummary = graphSummary;
        this.targetsSet = targetsSet;
        this.numOfThreads = numOfThreads;
        this.executor = Executors.newFixedThreadPool(numOfThreads);
        this.targetsList = new LinkedList<>();
        this.paused = false;
        this.stopped = false;
        this.statusChanged = false;
        this.incremental = incremental;
        this.pausedBefore = false;
        this.taskOutput = taskOutput;
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        String currTargetName = null;
        Target currTarget;
        taskPreparations();
        Boolean currentlyPaused = false;
        LinkedList<Future<?>> futures = new LinkedList<>();
        LinkedList<String> submitted = new LinkedList<>();

        if(this.targetsList.isEmpty())
        {
            Platform.runLater(() -> ShowPopUp("There are no targets available for the current task!", "Lack of available targets", Alert.AlertType.ERROR));
            return;
        }

        //Starting task on graph
        this.taskOutput.printStartOfTaskOnGraph(this.taskType);
        this.graphSummary.startTheClock();

        //Continuing polling targets while there are some left, and the "Stop" button didn't hit
        while(!getStopped() && (currentlyPaused || ((currTargetName = this.targetsList.poll()) != null)))
        {
            //If the task is on "pause"
            if(currentlyPaused)
            {
                //Pausing the clock if the "Pause" button just hit
                if(!this.pausedBefore)
                {
                    this.executor.shutdownNow();
                    while(!this.executor.isTerminated()) {}

                    for(int i = 0 ; i < submitted.size() ; ++i)
                    {
                        if(!futures.get(i).isDone())
                            this.targetsList.addFirst(submitted.get(i));
                    }
                    futures.clear();
                    submitted.clear();

                    this.pausedBefore = true;
                    this.graphSummary.pauseTheClock();
                }

                currentlyPaused = getPaused();
                continue;
            }

            currTarget = this.graph.getTarget(currTargetName);

            try {
                //Continuing the clock if the "Resume" button just hit
                if(this.pausedBefore)
                {
                    this.executor = Executors.newFixedThreadPool(this.numOfThreads);

                    this.pausedBefore = false;
                    this.graphSummary.continueTheClock();
                }

                if(this.graphSummary.isSkipped(currTargetName))
                    continue;
                else if(this.graphSummary.isTargetReadyToRun(currTarget, this.targetsSet) && this.graphSummary.checkIfSerialSetsAreOpen(currTarget.getSerialSets()))
                { //The target is ready to run!
                    this.graphSummary.addClosedSerialSets(currTarget);

                    if(this.taskType.equals(TaskType.Simulation))
                        futures.add(this.executor.submit(new SimulationThread(this.taskParametersMap.get(currTargetName), currTarget, this.graphSummary, this.taskOutput)));
                    else
                        futures.add(this.executor.submit(new CompilationThread(currTarget, this.graphSummary, this.compilationParameters, this.taskOutput)));
                    submitted.add(currTargetName);
                }
                else //The target is not ready to run yet, but not skipped either
                    this.targetsList.addLast(currTargetName);

                currentlyPaused = getPaused();

            } catch (FileNotFound | IOException | OpeningFileCrash e) {
                String finalCurrTarget = currTargetName;
                Platform.runLater(() -> ShowPopUp(e.getMessage(), "Error with " + finalCurrTarget + " file.", Alert.AlertType.ERROR));
            }
        }

        this.executor.shutdown();
        while (!this.executor.isTerminated()) { }

        this.graphSummary.stopTheClock();
        this.taskOutput.outputGraphSummary();
        Platform.runLater(() -> ShowPopUp("Task completed!\nCheck \"Task\" for more information.", "Task completed!", Alert.AlertType.INFORMATION));
    }

    private void ShowPopUp(String message, String title, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void taskPreparations()
    {
        TargetSummary currentTargetSummary;
        Target currentTarget;
        Boolean targetFrozen;
        TargetSummary.ResultStatus resultStatus;

        //Preparing the graph summary
        for(Target target : this.graph.getGraphTargets().values())
        {
            currentTargetSummary = this.graphSummary.getTargetsSummaryMap().get(target.getTargetName());
            resultStatus = currentTargetSummary.getResultStatus();
            currentTargetSummary.setRunning(this.targetsSet.contains(target.getTargetName())
                    && (!this.incremental || resultStatus.equals(TargetSummary.ResultStatus.Failure)));
            currentTargetSummary.setSkipped(false);
        }
        this.graphSummary.MakeNewClosedSerialSets();
        this.graphSummary.setSkippedTargetsToZero();
        //Finished preparing the graph summary

        //Initializing graph summary for current run
        for(String currentTargetName : this.targetsSet)
        {
            targetFrozen = false;
            currentTarget = this.graph.getTarget(currentTargetName);
            currentTargetSummary = this.graphSummary.getTargetsSummaryMap().get(currentTargetName);
            resultStatus = currentTargetSummary.getResultStatus();

            //Skipping the targets who finished successfully on the last run
            if(this.incremental && !resultStatus.equals(TargetSummary.ResultStatus.Failure) && !currentTargetSummary.isSkipped())
                continue;

            currentTargetSummary.setResultStatus(TargetSummary.ResultStatus.Undefined);

            for(String dependedTarget : currentTarget.getAllDependsOnTargets())
            {
                //Check if the current target has depends-on-targets in the current run (frozen)
                if(this.targetsSet.contains(dependedTarget))
                {
                    //Checking if the depended target will run in the current task (when incrementing)
                    resultStatus = this.graphSummary.getTargetsSummaryMap().get(dependedTarget).getResultStatus();
                    if(this.incremental && (resultStatus.equals(TargetSummary.ResultStatus.Success)
                            || resultStatus.equals(TargetSummary.ResultStatus.Warning)))
                    {
                        continue;
                    }
                    targetFrozen = true;
                    this.targetsList.addLast(currentTargetName);
                    this.graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Frozen, true);

                    break;
                }
            }
            if(!targetFrozen)
            {
                this.targetsList.addFirst(currentTargetName);
                this.graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting, true);
            }
        }
        //Finished initializing graph summary
    }

    public void setNumOfThreads(int numOfThreads) { this.numOfThreads = numOfThreads; }

    public Boolean getPaused() { return this.paused; }

    public Boolean getStopped() { return this.stopped; }

    public Boolean getStatusChanged() { return this.statusChanged; }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public void stopTheTask()
    {
        this.stopped = this.statusChanged = true;
        this.executor.shutdownNow();
    }

    public void pauseTheTask() { this.paused = this.statusChanged = true; }

    public void continueTheTask() {
        this.paused = false;
        this.statusChanged = true;
    }

    public void resetStatusChanged() { this.statusChanged = false; }
}