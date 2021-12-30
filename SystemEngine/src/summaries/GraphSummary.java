package summaries;

import target.Graph;
import target.Target;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GraphSummary implements Serializable {
    //--------------------------------------------------Members-----------------------------------------------------//
    private final String graphName;
    private Duration totalTime;
    private Instant timeStarted;
    private final Map<String, TargetSummary> targetsSummaryMap;
    private Map<TargetSummary.ResultStatus, Integer> allResultStatus;
    private Boolean firstRun;
    private Integer skippedTargets;
    private final String workingDirectory;

    //------------------------------------------------Constructors--------------------------------------------------//
    public GraphSummary(Graph graph, String workingDirectory) {
        this.targetsSummaryMap = new HashMap<>();
        this.firstRun = true;
        this.graphName = graph.getGraphName();
        this.workingDirectory = workingDirectory;

        TargetSummary currentTargetSummary;
        for(Target currentTarget : graph.getGraphTargets().values())
        {
            currentTargetSummary = new TargetSummary(currentTarget.getTargetName());

            if(currentTarget.getTargetPosition().equals(Target.TargetPosition.ROOT))
                currentTargetSummary.setRoot(true);

            this.targetsSummaryMap.put(currentTarget.getTargetName(), currentTargetSummary);
        }
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public Boolean getFirstRun() {
        return this.firstRun;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public Map<TargetSummary.ResultStatus, Integer> getAllResultStatus() {
        return this.allResultStatus;
    }

    public Duration getTime() {
        return this.totalTime;
    }

    public Map<String, TargetSummary> getTargetsSummaryMap() {
        return this.targetsSummaryMap;
    }

    public Integer getSkippedTargets() { return this.skippedTargets; }

    public String getWorkingDirectory() { return workingDirectory; }

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setFirstRun(Boolean firstRun) {
        this.firstRun = firstRun;
    }

    public void setRunningTargets(Target currentTarget, Boolean runningOrNot)
    {
        this.targetsSummaryMap.get(currentTarget.getTargetName()).setRunning(runningOrNot);

        for(Target requiredForTarget : currentTarget.getRequiredForTargets())
            setRunningTargets(requiredForTarget, runningOrNot);
    }

    public synchronized void UpdateTargetSummary(Target target, TargetSummary.ResultStatus resultStatus, TargetSummary.RuntimeStatus runtimeStatus)
    {
        TargetSummary targetSummary = targetsSummaryMap.get(target.getTargetName());

        targetSummary.setResultStatus(resultStatus);
        targetSummary.setRuntimeStatus(runtimeStatus);

        if(resultStatus.equals(TargetSummary.ResultStatus.Failure))
            setAllRequiredForTargetsOnSkipped(target, target);
    }

    public synchronized Boolean isTargetReadyToRun(Target target, Set<String> runningTargets)
    {
        TargetSummary targetSummary = targetsSummaryMap.get(target.getTargetName());

        for(Target dependedTarget : target.getDependsOnTargets())
        {
            //The depended target is not "in the game"
            if(!runningTargets.contains(dependedTarget.getTargetName()))
                continue;

            TargetSummary dependedTargetSummary = getTargetsSummaryMap().get(dependedTarget.getTargetName());

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

    public void setAllRequiredForTargetsOnSkipped(Target failedTarget, Target lastSkippedTarget)
    {
        TargetSummary newSkippedTargetSummary;
        for(Target newSkippedTarget : lastSkippedTarget.getRequiredForTargets())
        {
            newSkippedTargetSummary = targetsSummaryMap.get(newSkippedTarget.getTargetName());

            if(newSkippedTargetSummary.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped))
                continue;

            newSkippedTargetSummary.setRuntimeStatus(TargetSummary.RuntimeStatus.Skipped);
            newSkippedTargetSummary.setResultStatus(TargetSummary.ResultStatus.Failure);
            newSkippedTargetSummary.addNewSkippedByTarget(failedTarget.getTargetName());

            setAllRequiredForTargetsOnSkipped(failedTarget, lastSkippedTarget);
        }
    }

    public Boolean isSkipped(String targetName)
    {
        return targetsSummaryMap.get(targetName).getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void startTheClock()
    {
        this.timeStarted = Instant.now();
    }

    public void stopTheClock()
    {
        Instant timeEnded = Instant.now();
        this.totalTime = Duration.between(this.timeStarted, timeEnded);
    }

    public void calculateResults()
    {
        this.allResultStatus = new HashMap<>();
        Integer succeeded = 0, failed = 0, warning = 0;

        for(TargetSummary current : this.targetsSummaryMap.values())
        {
//            if(!current.isRunning())
//                continue;

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


}
