package task;

import java.util.LinkedList;
import java.util.Set;

public class CompilationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private final Set<String> allTargets;
    private final Integer pricingForTarget;
    private final CompilationParameters parameters;
    private String taskLog;
    private final LinkedList<String> waitingTargets;

    public CompilationTaskInformation(String taskName, String taskCreator, String graphName, Set<String> allTargets, Integer pricingForTarget, CompilationParameters parameters) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.allTargets = allTargets;
        this.pricingForTarget = pricingForTarget;
        this.parameters = parameters;
        this.waitingTargets = new LinkedList<>();
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getTaskCreator() {
        return this.taskCreator;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public Set<String> getTargetsToExecute() {
        return this.allTargets;
    }

    public Integer getPricingForTarget() {
        return this.pricingForTarget;
    }

    public CompilationParameters getCompilationParameters() {
        return this.parameters;
    }

    public void updateLog(String newInfo) { this.taskLog = newInfo; }

    public String getTaskLog() { return this.taskLog; }

    public synchronized void addTargetToWaitingList(String waitingTarget) { this.waitingTargets.addLast(waitingTarget); }

    public synchronized String getTargetToExecute() { return this.waitingTargets.poll(); }
}