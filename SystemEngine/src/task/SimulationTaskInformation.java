package task;

import java.util.Set;

public class SimulationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private final Set<String> allTargets;
    private final Integer pricingForTarget;
    private final SimulationParameters parameters;
    private String taskLog;

    public SimulationTaskInformation(String taskName, String taskCreator, String graphName, Set<String> allTargets, Integer pricingForTarget, SimulationParameters parameters) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.allTargets = allTargets;
        this.pricingForTarget = pricingForTarget;
        this.parameters = parameters;
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

    public SimulationParameters getSimulationParameters() {
        return this.parameters;
    }

    public synchronized void updateLog(String newInfo) { this.taskLog = newInfo; }

    public synchronized String getTaskLog() { return this.taskLog; }
}