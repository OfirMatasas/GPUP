package summaries;

import target.Graph;
import target.Target;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphSummary {
    //--------------------------------------------------Members-----------------------------------------------------//
    private final String graphName;
    private Duration totalTime;
    private Instant timeStarted;
    private final Map<String, TargetSummary> targetsSummaryMap;
    private Map<TargetSummary.ResultStatus, Integer> allResultStatus;
    private Integer skippedTargets;
    private Instant timePaused;
    private Duration totalPausedTime;

    //------------------------------------------------Constructors--------------------------------------------------//
    public GraphSummary(Graph graph) {
        this.targetsSummaryMap = new HashMap<>();
        this.graphName = graph.getGraphName();
        this.totalPausedTime = Duration.ZERO;
        this.timePaused = null;

        TargetSummary currentTargetSummary;
        for(Target currentTarget : graph.getGraphTargets().values())
        {
            currentTargetSummary = new TargetSummary(currentTarget.getTargetName());

            this.targetsSummaryMap.put(currentTarget.getTargetName(), currentTargetSummary);
        }
    }

    public GraphSummary(GraphSummary graphSummary) {
        this.targetsSummaryMap = new HashMap<>();
        this.allResultStatus = new HashMap<>();
        this.graphName = graphSummary.getGraphName();
        this.totalPausedTime = Duration.ZERO;
        this.timePaused = null;

        TargetSummary copiedSummary;
        for(TargetSummary curr : graphSummary.getTargetsSummaryMap().values())
        {
            copiedSummary = new TargetSummary(curr);
            this.targetsSummaryMap.put(copiedSummary.getTargetName(), copiedSummary);
        }

        this.allResultStatus.put(TargetSummary.ResultStatus.Undefined, graphSummary.allResultStatus.get(TargetSummary.ResultStatus.Undefined));
        this.allResultStatus.put(TargetSummary.ResultStatus.Success, graphSummary.allResultStatus.get(TargetSummary.ResultStatus.Success));
        this.allResultStatus.put(TargetSummary.ResultStatus.Warning, graphSummary.allResultStatus.get(TargetSummary.ResultStatus.Warning));
        this.allResultStatus.put(TargetSummary.ResultStatus.Failure, graphSummary.allResultStatus.get(TargetSummary.ResultStatus.Failure));
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getGraphName() { return this.graphName; }

    public Map<TargetSummary.ResultStatus, Integer> getAllResultStatus() { return this.allResultStatus; }

    public Duration getTime() { return this.totalTime; }

    public Map<String, TargetSummary> getTargetsSummaryMap() { return this.targetsSummaryMap; }

    public Integer getSkippedTargets() { return this.skippedTargets; }

    public void setSkippedTargetsToZero() { this.skippedTargets = 0; }

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setRunningTargets(Target currentTarget, Boolean runningOrNot) {
        this.targetsSummaryMap.get(currentTarget.getTargetName()).setRunning(runningOrNot);

        for(Target requiredForTarget : currentTarget.getRequiredForTargets())
            setRunningTargets(requiredForTarget, runningOrNot);
    }

    public void setAllRequiredForTargetsOnSkipped(Target failedTarget, Target lastSkippedTarget) {
        TargetSummary newSkippedTargetSummary;
        for(Target newSkippedTarget : lastSkippedTarget.getRequiredForTargets())
        {
            newSkippedTargetSummary = this.targetsSummaryMap.get(newSkippedTarget.getTargetName());

            if(newSkippedTargetSummary.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped))
                continue;

            newSkippedTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Skipped);
            newSkippedTargetSummary.setResultStatus(TargetSummary.ResultStatus.Failure);
            newSkippedTargetSummary.setSkipped(true);
            newSkippedTargetSummary.addNewSkippedByTarget(failedTarget.getTargetName());

            setAllRequiredForTargetsOnSkipped(failedTarget, newSkippedTarget);
        }
    }

    public synchronized Boolean isSkipped(String targetName) {
        return this.targetsSummaryMap.get(targetName).getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void startTheClock()
    {
        this.timeStarted = Instant.now();
    }

    public void stopTheClock() {
        Instant timeEnded = Instant.now();

        if(this.timePaused != null)
            continueTheClock();

        this.totalTime = Duration.between(this.timeStarted, timeEnded).minus(this.totalPausedTime);
        this.totalPausedTime = Duration.ZERO;
    }

    public void pauseTheClock() {
        this.timePaused = Instant.now();

        for(TargetSummary curr : this.targetsSummaryMap.values())
        {
            if(curr.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Waiting))
                curr.pausingWaitingTime();
        }
    }

    public void continueTheClock() {
        Instant timeEnded = Instant.now();

        this.totalPausedTime = Duration.between(this.timePaused, timeEnded).plus(this.totalPausedTime);
        this.timePaused = null;

        for(TargetSummary curr : this.targetsSummaryMap.values())
        {
            if(curr.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Waiting))
                curr.continuingWaitingTime();
        }
    }

    public void calculateResults() {
        this.allResultStatus = new HashMap<>();
        Integer succeeded = 0, failed = 0, warning = 0;

        for(TargetSummary current : this.targetsSummaryMap.values())
        {
            if(!current.isRunning())
                continue;

            if(current.isSkipped())
            {
                this.skippedTargets++;
                continue;
            }

            switch (current.getResultStatus())
            {
                case Failure:
                {
                    failed++;
                    break;
                }
                case Success:
                {
                    succeeded++;
                    break;
                }
                case Warning:
                {
                    warning++;
                    break;
                }
            }
        }

        this.allResultStatus.put(TargetSummary.ResultStatus.Success, succeeded);
        this.allResultStatus.put(TargetSummary.ResultStatus.Failure, failed);
        this.allResultStatus.put(TargetSummary.ResultStatus.Warning, warning);
    }

    public synchronized void UpdateTargetSummary(Target target, TargetSummary.ResultStatus resultStatus, TargetSummary.RuntimeStatus runtimeStatus) {
        TargetSummary targetSummary = this.targetsSummaryMap.get(target.getTargetName());
        targetSummary.setResultStatus(resultStatus);
        targetSummary.setRuntimeStatus(runtimeStatus);

        if(runtimeStatus.equals(TargetSummary.RuntimeStatus.Waiting))
            targetSummary.startWaitingTime();
        else if(runtimeStatus.equals(TargetSummary.RuntimeStatus.InProcess))
        {
            targetSummary.startTheClock();
            targetSummary.startProcessingTime();
        }
        else if(resultStatus.equals(TargetSummary.ResultStatus.Failure))
            setAllRequiredForTargetsOnSkipped(target, target);

        if(runtimeStatus.equals(TargetSummary.RuntimeStatus.Finished))
            targetSummary.stopTheClock();
    }

    public synchronized Boolean isTargetReadyToRun(Target target, Set<String> runningTargets) {
        if(this.targetsSummaryMap.get(target.getTargetName()).getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Waiting))
            return true;

        for(String dependedTargetName : target.getAllDependsOnTargets())
        {
            //The depended target is not "in the game"
            if(!runningTargets.contains(dependedTargetName))
                continue;

            TargetSummary dependedTargetSummary = getTargetsSummaryMap().get(dependedTargetName);

            if(dependedTargetSummary.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished))
            {
                if(dependedTargetSummary.getResultStatus().equals(TargetSummary.ResultStatus.Failure))
                    return false;
                //The target finished with success / with warnings
                continue;
            }
            //The target is not finished its run
            return false;
        }

        //Runnable
        UpdateTargetSummary(target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.Waiting);
        return true;
    }

    public synchronized String getRunningTargetInfo(Target target, Set<String> runningTargets) {
        String targetName = target.getTargetName();
        TargetSummary targetSummary = this.targetsSummaryMap.get(targetName);
        String runningDetails;

        runningDetails = "Target: " + targetName + "\n";
        runningDetails += "Position: " + target.getTargetPosition() + "\n";
        runningDetails += "Runtime status: ";

        switch (targetSummary.getRuntimeStatus())
        {
            case Frozen:
            {
                runningDetails += "Frozen, until the following targets will be executed: ";
                runningDetails += getWaitingForTargets(target, runningTargets);
                break;
            }
            case Skipped:
            {
                runningDetails += "Skipped\n";
                runningDetails += "List of dependencies that their process failed are : ";
                runningDetails += getFailedDependsOnTargets(target, runningTargets);
                break;
            }
            case Waiting:
            {
                runningDetails += "Waiting, for: " +
                        (targetSummary.currentWaitingTime().toMillis() - targetSummary.getTotalPausingTime().toMillis()) + "m/s\n";
                break;
            }
            case InProcess:
            {
                runningDetails += "In process, for: " + targetSummary.currentProcessingTime().toMillis() + "m/s\n";
                break;
            }
            case Finished:
            {
                runningDetails += "Finished.\n";
                runningDetails += "Result status: ";

                if(targetSummary.isSkipped())
                    runningDetails += "Skipped\n";
                else
                    runningDetails += targetSummary.getResultStatus() + "\n";

                if(!targetSummary.isSkipped())
                    runningDetails += "Running time: " + targetSummary.getTime().toMillis() + "m/s\n";
                break;
            }
        }
        return runningDetails;
    }

    public String getWaitingForTargets(Target target, Set<String> runningTargets)
    {
        String waitingForTargets = "";
        Set<String> allDependedTargets = target.getAllDependsOnTargets();
        TargetSummary dependedTargetSummary;

        for(String currRunningTargetName : runningTargets) //Checking for depended-on targets which are in the task
        {
            if(allDependedTargets.contains(currRunningTargetName)) //Found a depended target which is on current task
            {
                dependedTargetSummary = this.targetsSummaryMap.get(currRunningTargetName);
                //If the depended-on target is not finished yet - success
                if(!dependedTargetSummary.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished))
                    waitingForTargets += currRunningTargetName + " ";
            }
        }

        return waitingForTargets.equals("") ? "none.\n" : waitingForTargets + "\n";
    }

    public String getFailedDependsOnTargets(Target target, Set<String> runningTargets)
    {
        String failedDependsOnTargets = "";
        Set<String> dependedTargets = target.getAllDependsOnTargets();
        TargetSummary dependedTargetSummary;

        for(String currRunningTargetName : runningTargets)
        {
            if (dependedTargets.contains(currRunningTargetName))
            {
                dependedTargetSummary = this.targetsSummaryMap.get(currRunningTargetName);

                if(dependedTargetSummary.getResultStatus().equals(TargetSummary.ResultStatus.Failure))
                    failedDependsOnTargets += currRunningTargetName + " ";
            }
        }

        return failedDependsOnTargets.equals("") ? "none.\n" : failedDependsOnTargets + "\n";
    }
}
