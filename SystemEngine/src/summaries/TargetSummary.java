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
    private Instant ProcessingTimeStarted;
    private Duration waitingTime;
    private Duration processingTime;

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
        this.waitingTime = Duration.ZERO;
        this.processingTime = Duration.ZERO;
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

    public Set<String> getSkippedByTargets() {
        return skippedByTargets;
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

    public synchronized boolean isSkipped() {
        return this.isSkipped;
    }

    public Set<String> getOpenedTargets() {
        return openedTargets;
    }

    public Boolean getRoot() {
        return this.isRoot;
    }

    public Duration getWaitingTime() { return waitingTime; }

    public Duration getProcessingTime() { return processingTime; }

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
        isSkipped = skipped;
    }

    public void setResultStatus(ResultStatus resultStatus) { this.resultStatus = resultStatus; }

    public void setRoot(Boolean root) {
        isRoot = root;
    }

    public void setOpenedTargetsToZero()
    {
        this.openedTargets.clear();
    }

    public void setWaitingTime(Duration waitingTime) { this.waitingTime = waitingTime; }

    public void setProcessingTime(Duration processingTime) { this.processingTime = processingTime; }


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

//    public Boolean checkIfFailedBefore()
//    {
//        if(skippedTargets != null)
//            return true;
//
//        skippedTargets = new HashSet<>();
//        return false;
//    }

    public void addNewSkippedByTarget(String skippedByTargetName)
    {
        if(skippedByTargets == null)
            skippedByTargets = new HashSet<String>();

        skippedByTargets.add(skippedByTargetName);
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

    public void startWaitingTime()
    {
        waitingTimeStarted = Instant.now();
    }

    public Duration currentWaitingTime()
    {
        Instant timeNow = Instant.now();
        return Duration.between(waitingTimeStarted, timeNow);
    }

    public void startProcessingTime()
    {
        //stopping waiting time , the target is in processing time
        Instant waitingTimeEnded = Instant.now();
        waitingTime = Duration.between(waitingTimeStarted, waitingTimeEnded);

        //starting processing time
        ProcessingTimeStarted = Instant.now();
    }

    public Duration currentProcessingTime()
    {
        Instant timeNow = Instant.now();
        return Duration.between(ProcessingTimeStarted, timeNow);
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
        return Objects.equals(targetName, that.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetName);
    }
}
