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
    public enum RuntimeStatus { Frozen, Skipped, Waiting, InProcess, Finished , Undefined }
    public enum ResultStatus { Success, Warning, Failure, Undefined }

    //--------------------------------------------------Members-----------------------------------------------------//
    private Duration actualTime, predictedTime;
    private final String targetName;
    private final String extraInformation;
    private ResultStatus resultStatus;
    private RuntimeStatus runtimeStatus;
    private boolean isSkipped, running;
    private Instant timeStarted;
    private Set<String> skippedByTargets;
    private final Set<String> openedTargets;
    private Instant waitingTimeStarted;
    private Instant processingTimeStarted;
    private Duration waitingTime;
    private Duration processingTime;
    private Instant pausingTime;
    private Duration totalPausingTime;

    //------------------------------------------------Constructors--------------------------------------------------//
    public TargetSummary(String targetName) {
        this.targetName = targetName;
        this.actualTime = Duration.ZERO;
        this.predictedTime = Duration.ZERO;
        this.extraInformation = null;
        this.resultStatus = ResultStatus.Undefined;
        this.runtimeStatus = RuntimeStatus.Undefined;
        this.isSkipped = false;
        this.openedTargets = new HashSet<>();
        this.isRoot = false;
        this.waitingTime = Duration.ZERO;
        this.processingTime = Duration.ZERO;
        this.totalPausingTime = Duration.ZERO;
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getTargetName() {
        return this.targetName;
    }

    public String getExtraInformation() {
        return this.extraInformation;
    }

    public boolean isRunning() {
        return this.running;
    }

    public Set<String> getSkippedByTargets() {
        return this.skippedByTargets;
    }

    public Duration getPredictedTime() {
        return this.predictedTime;
    }

    public RuntimeStatus getRuntimeStatus() {
        return this.runtimeStatus;
    }

    public Duration getTime() {
        return this.actualTime;
    }

    public ResultStatus getResultStatus() {
        return this.resultStatus;
    }

    public synchronized boolean isSkipped() {
        return this.isSkipped;
    }

    public Set<String> getOpenedTargets() {
        return this.openedTargets;
    }

    public Boolean getRoot() {
        return this.isRoot;
    }

    public Duration getProcessingTime() { return this.processingTime; }

    public Instant getTimeStarted() {
        return this.timeStarted;
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

    public synchronized void setSkipped(boolean skipped) {
        this.isSkipped = skipped;
    }

    public void setResultStatus(ResultStatus resultStatus) { this.resultStatus = resultStatus; }

    public void setRoot(Boolean root) {
        this.isRoot = root;
    }

    public void setOpenedTargetsToZero()
    {
        this.openedTargets.clear();
    }

    public void setProcessingTime(Duration processingTime) { this.processingTime = processingTime; }


    //--------------------------------------------------Methods-----------------------------------------------------//
    public void startTheClock()
    {
        this.timeStarted = Instant.now();
    }

    public void stopTheClock()
    {
        Instant timeEnded = Instant.now();
        this.actualTime = Duration.between(this.timeStarted, timeEnded);
    }

    public void addNewSkippedByTarget(String skippedByTargetName)
    {
        if(this.skippedByTargets == null)
            this.skippedByTargets = new HashSet<>();

        this.skippedByTargets.add(skippedByTargetName);
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

            this.openedTargets.add(requiredForTarget.getTargetName());
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

    public void startWaitingTime()
    {
        this.totalPausingTime = Duration.ZERO;
        this.waitingTimeStarted = Instant.now();
    }

    public Duration currentWaitingTime()
    {
        Instant timeNow = Instant.now();

        if(this.pausingTime != null)
            return Duration.between(this.waitingTimeStarted, this.pausingTime);
        else
            return Duration.between(this.waitingTimeStarted ,timeNow);
    }

    public void pausingWaitingTime()
    {
        this.pausingTime = Instant.now();
    }

    public void continuingWaitingTime()
    {
        Instant resumeTime = Instant.now();

        this.totalPausingTime = Duration.between(this.pausingTime, resumeTime).plus(this.totalPausingTime);
        this.pausingTime = null;
    }

    public Duration getTotalPausingTime() {
        return totalPausingTime;
    }

    public void startProcessingTime()
    {
        //stopping waiting time , the target is in processing time
        Instant waitingTimeEnded = Instant.now();
        this.waitingTime = Duration.between(this.waitingTimeStarted, waitingTimeEnded);

        //starting processing time
        this.processingTimeStarted = Instant.now();
    }

    public Duration currentProcessingTime()
    {
        Instant timeNow = Instant.now();
        return Duration.between(this.processingTimeStarted, timeNow);
    }

    public void startFinishingTime()
    {
        //Instant processingTimeEnded = Instant.now();
        //processingTime = Duration.between(ProcessingTimeStarted,processingTimeEnded);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetSummary that = (TargetSummary) o;
        return Objects.equals(this.targetName, that.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.targetName);
    }
}
