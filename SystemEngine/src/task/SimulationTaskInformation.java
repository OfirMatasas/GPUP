package task;

import java.util.Set;

public class SimulationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private final Set<String> targetsToExecute;
    private final Integer pricingForTarget;
    private final SimulationParameters simulationParameters;
    private String taskLog;

    public SimulationTaskInformation(String taskName, String taskCreator, String graphName, Set<String> targetsToExecute, Integer pricingForTarget, SimulationParameters simulationParameters) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.targetsToExecute = targetsToExecute;
        this.pricingForTarget = pricingForTarget;
        this.simulationParameters = simulationParameters;
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
        return this.targetsToExecute;
    }

    public Integer getPricingForTarget() {
        return this.pricingForTarget;
    }

    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    public void updateLog(String newInfo) { this.taskLog = newInfo; }

    public String getTaskLog() { return this.taskLog; }
}