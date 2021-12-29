package summaries;

import target.Target;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TargetSummary implements Serializable
{
    private Boolean isRoot;
    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum RuntimeStatus { Frozen, Skipped, Waiting, InProcess, Finished }
    public enum ResultStatus { Success, Warning, Failure }

    //--------------------------------------------------Members-----------------------------------------------------//
    private Duration actualTime, predictedTime;
    private final String targetName;
    private final String extraInformation;
    private ResultStatus resultStatus;
    private RuntimeStatus runtimeStatus;
    private boolean isSkipped, running;
    private Instant timeStarted;
    private Set<String> skippedTargets;
    private final Set<String> openedTargets;

    //------------------------------------------------Constructors--------------------------------------------------//
    public TargetSummary(String targetName) {
        this.targetName = targetName;
        this.actualTime = Duration.ZERO;
        this.predictedTime = Duration.ZERO;
        this.extraInformation = null;
        this.resultStatus = ResultStatus.Failure;
        this.runtimeStatus = RuntimeStatus.Frozen;
        this.isSkipped = false;
        this.openedTargets = new HashSet<>();
        this.isRoot = false;
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getTargetName() {
        return targetName;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public boolean isRunning() {
        return running;
    }

    public Set<String> getSkippedTargets() {
        return skippedTargets;
    }

    public Duration getPredictedTime() {
        return predictedTime;
    }

    public RuntimeStatus getRuntimeStatus() {
        return runtimeStatus;
    }

    public Duration getTime() {
        return actualTime;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public boolean isSkipped() {
        return this.isSkipped;
    }

    public Set<String> getOpenedTargets() {
        return openedTargets;
    }

    public Boolean getRoot() {
        return this.isRoot;
    }

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setPredictedTime(Duration predictedTime) {
        this.predictedTime = predictedTime;
    }

    public void setRuntimeStatus(RuntimeStatus runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
    }

    public void setSkipped(boolean skipped) {
        isSkipped = skipped;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public void setRoot(Boolean root) {
        isRoot = root;
    }

    public void setOpenedTargetsToZero()
    {
        this.openedTargets.clear();
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void startTheClock()
    {
        timeStarted = Instant.now();
    }

    public void stopTheClock()
    {
        Instant timeEnded = Instant.now();
        actualTime = Duration.between(timeStarted, timeEnded);
    }

    public Boolean checkIfFailedBefore()
    {
        if(skippedTargets == null)
        {
            skippedTargets = new HashSet<>();
            return false;
        }

        return true;
    }

    public void addNewSkippedTarget(String skippedTargetName)
    {
        skippedTargets.add(skippedTargetName);
    }

    public void checkForOpenTargets(Target executedTarget, GraphSummary graphSummary)
    {
        TargetSummary dependsOnTargetSummary;
        Boolean skip;

        for(Target requiredForTarget : executedTarget.getRequiredForTargets())
        {
            skip = false;
            for(Target dependsOnTarget : requiredForTarget.getDependsOnTargets())
            {
                dependsOnTargetSummary = graphSummary.getTargetsSummaryMap().get(dependsOnTarget.getTargetName());

                if(!dependsOnTargetSummary.getRuntimeStatus().equals(RuntimeStatus.Finished))
                {
                    skip = true;
                    break;
                }
            }

            if(skip)
                continue;

            openedTargets.add(requiredForTarget.getTargetName());
        }
    }

    public void setAllRequiredForTargetsRuntimeStatus(Target target, GraphSummary graphSummary, RuntimeStatus runtimeStatus)
    {
        for(Target requiredForTarget : target.getRequiredForTargets())
        {
            graphSummary.getTargetsSummaryMap().get(requiredForTarget.getTargetName()).setRuntimeStatus(runtimeStatus);
            setAllRequiredForTargetsRuntimeStatus(requiredForTarget, graphSummary, runtimeStatus);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetSummary that = (TargetSummary) o;
        return Objects.equals(targetName, that.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetName);
    }
}
