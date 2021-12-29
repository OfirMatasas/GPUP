package summaries;

import target.Graph;
import target.Target;
import task.TaskParameters;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class GraphSummary implements Serializable {
    //--------------------------------------------------Members-----------------------------------------------------//
    private final String graphName;
    private Duration totalTime;
    private Instant timeStarted;
    private Map<String, TargetSummary> targetsSummaryMap;
    private Map<TargetSummary.ResultStatus, Integer> allResultStatus;
    private Boolean firstRun;
    private Integer skippedTargets;
    private String workingDirectory;

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

    public void setAllRequiredForTargetsOnSkipped(Target lastSkippedTarget, TargetSummary failedTargetSummary)
    {
        TargetSummary targetSummary;

        for(Target newSkippedTarget : lastSkippedTarget.getRequiredForTargets())
        {
            targetSummary = this.targetsSummaryMap.get(newSkippedTarget.getTargetName());

            if(!targetSummary.isSkipped())
            {
                this.skippedTargets++;
                targetSummary.setSkipped(true);
            }

            failedTargetSummary.addNewSkippedTarget(newSkippedTarget.getTargetName());
            setAllRequiredForTargetsOnSkipped(newSkippedTarget, failedTargetSummary);
        }
    }

    public void setSkippedTargetsToZero()
    {
        this.skippedTargets = 0;
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
            if(!current.isRunning())
                continue;

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
        this.allResultStatus.put(TargetSummary.ResultStatus.Failure, failed - this.skippedTargets);
        this.allResultStatus.put(TargetSummary.ResultStatus.Warning, warning);
    }

    public void changePredictedTime(Graph graph, Map<Target, TaskParameters> targetsParameters) {
        Target currentTarget;
        Long timeLong;
        Duration timeDuration, originalTime = TaskParameters.getProcessingTime();
        TaskParameters currentTaskParameters;

        for(TargetSummary currentTargetSummary : targetsSummaryMap.values())
        {
            currentTarget = graph.getTarget(currentTargetSummary.getTargetName());
            currentTaskParameters = targetsParameters.get(currentTarget);
            timeDuration = originalTime;

            if(targetsParameters.get(currentTarget).isRandom())
            {
                timeLong = (long)(Math.random() * (originalTime.toMillis())) + 1;
                timeDuration = Duration.of(timeLong, ChronoUnit.MILLIS);
                currentTargetSummary.setPredictedTime(timeDuration);
            }
        }
    }
}
