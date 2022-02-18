package task;

import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TaskThread extends Thread {

    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum TaskType { Simulation, Compilation }

    //-------------------------------------------------Members------------------------------------------------------//
    //Getting from UI
    private final Graph graph;
    private final TaskType taskType;
    private final SimulationTaskInformation simulationTask;
    private final CompilationTaskInformation compilationTask;
    private final GraphSummary graphSummary;
    private final Set<String> targetsSet;
    private final String creator;
    private final Boolean incremental;
    private final String taskName;

    //Local use
    private final BlockingQueue<String> targetsList;
    private final BlockingQueue<String> waitingTargetsList;
    private final BlockingQueue<String> sentTargetsList;
    private final BlockingQueue<String> finishedTargets;
    private Boolean paused;
    private Boolean stopped;
    private Boolean pausedBefore;
    private Boolean statusChanged;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(String taskName, Graph graph, Set<String> targetsSet, GraphSummary graphSummary, SimulationTaskInformation simulationTask,
                      CompilationTaskInformation compilationTask, Boolean incremental) {
        this.taskName = taskName;
        this.graph = graph;
        this.simulationTask = simulationTask;
        this.compilationTask = compilationTask;
        this.graphSummary = graphSummary;
        this.incremental = incremental;
        this.targetsSet = targetsSet;

        if(this.simulationTask != null) //Simulation task
        {
            this.taskType = TaskType.Simulation;
            this.creator = this.simulationTask.getTaskCreator();
        }
        else //Compilation task
        {
            this.taskType = TaskType.Compilation;
            this.creator = compilationTask.getTaskCreator();
        }

        this.paused = false;
        this.stopped = false;
        this.statusChanged = false;
        this.pausedBefore = false;
        this.targetsList = new ArrayBlockingQueue<>(this.targetsSet.size());
        this.waitingTargetsList = new ArrayBlockingQueue<>(this.targetsSet.size());
        this.sentTargetsList = new ArrayBlockingQueue<>(this.targetsSet.size());
        this.finishedTargets = new ArrayBlockingQueue<>(this.targetsSet.size());

//        try {
//            this.taskOutput.createNewDirectoryOfTaskLogs(taskType);
//        } catch (OpeningFileCrash e) {
//            e.printStackTrace();
//        }
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        Thread.currentThread().setName(this.taskName);
        String currTargetName = null;
        Target currTarget;
        taskPreparations();
        Boolean currentlyPaused = false, finished = false;

        //Starting task on graph
//        this.taskOutput.printStartOfTaskOnGraph(this.taskType);
        this.graphSummary.startTheClock();

        //Continuing polling targets while there are some left, and the "Stop" button didn't hit
        while(!finished && !getStopped())
        {
            //If the task is on "pause"
            if(currentlyPaused)
            {
                //Pausing the clock if the "Pause" button just hit
                if(!this.pausedBefore)
                {
                    this.pausedBefore = true;
                    this.graphSummary.pauseTheClock();
                }

                currentlyPaused = getPaused();
                continue;
            }

            //If the task is waiting for updates from workers
            if(this.targetsList.isEmpty())
            {
                finished = this.targetsSet.size() == this.finishedTargets.size();
                continue;
            }

            currTargetName = this.targetsList.poll();
            currTarget = this.graph.getTarget(currTargetName);

            //Continuing the clock if the "Resume" button just hit
            if(this.pausedBefore)
            {
                this.pausedBefore = false;
                this.graphSummary.continueTheClock();
            }

            if(this.graphSummary.isSkipped(currTargetName)) //The target is skipped
                continue;
            else if(this.graphSummary.isTargetReadyToRun(currTarget, this.targetsSet)) //The target is ready to run!
                this.waitingTargetsList.add(currTargetName);
            else //The target is not ready to run yet, but not skipped either
                this.targetsList.add(currTargetName);

            currentlyPaused = getPaused();
            finished = this.targetsSet.size() == this.finishedTargets.size();
        }

        this.graphSummary.stopTheClock();
//        this.taskOutput.outputGraphSummary();
    }

    private void taskPreparations()
    {
        TargetSummary currentTargetSummary;
        Target currentTarget;
        boolean targetFrozen;
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
                    this.targetsList.add(currentTargetName);
                    this.graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Frozen);

                    break;
                }
            }
            if(!targetFrozen)
            {
                this.targetsList.add(currentTargetName);
                this.graphSummary.UpdateTargetSummary(currentTarget, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting);
            }
        }
        //Finished initializing graph summary
    }

    public Boolean getPaused() { return this.paused; }

    public Boolean getStopped() { return this.stopped; }

    public Boolean getStatusChanged() { return this.statusChanged; }

    public void stopTheTask()
    {
        this.stopped = this.statusChanged = true;
    }

    public void pauseTheTask() { this.paused = this.statusChanged = true; }

    public void continueTheTask() {
        this.paused = false;
        this.statusChanged = true;
    }

    public void resetStatusChanged() { this.statusChanged = false; }

    public String getCreator() { return this.creator; }

    public BlockingQueue<String> getWaitingTargetsList() { return this.targetsList; }

    public synchronized Set<String> returnTargetsToWaitingList(Set<String> targets) {
        Set<String> abortedTargets = new HashSet<>();

        for(String currTarget : targets)
        {
            if(this.sentTargetsList.contains(currTarget))
            {
                abortedTargets.add(currTarget);
                returnTargetToWaitingList(currTarget);
            }
        }

        return abortedTargets;
    }

    public synchronized String getWaitingTargetToExecute() {
        String targetName = null;
        if(!this.waitingTargetsList.isEmpty())
        {
            targetName = this.waitingTargetsList.poll();
            this.sentTargetsList.add(targetName);
        }

        return targetName;
    }

    public void taskOnTargetFinished(String targetName) {
        this.sentTargetsList.remove(targetName);
        this.finishedTargets.add(targetName);
    }

    public void returnTargetToWaitingList(String targetName)
    {
        this.sentTargetsList.remove(targetName);
        this.waitingTargetsList.add(targetName);
    }
}