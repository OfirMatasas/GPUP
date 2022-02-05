package task;

import java.util.Set;

public class CompilationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private Set<String> targetsToExecute;
    private Integer pricingForTarget;

    public CompilationTaskInformation(String taskName, String taskCreator, String graphName, Set<String> targetsToExecute, Integer pricingForTarget) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.targetsToExecute = targetsToExecute;
        this.pricingForTarget = pricingForTarget;
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
}
