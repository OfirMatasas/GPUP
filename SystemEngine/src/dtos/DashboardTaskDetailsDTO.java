package dtos;

import target.Graph;
import target.Target;

import java.util.Map;
import java.util.Set;

public class DashboardTaskDetailsDTO {

    private final String taskName;
    private final String graphName;
    private final String uploader;
    private final Integer targets;
    private final Integer roots;
    private final Integer middles;
    private final Integer leaves;
    private final Integer independents;
    private final Integer totalPayment;
    private Integer totalWorkers;
    private String taskStatus;

    public DashboardTaskDetailsDTO(String taskName, Graph graph) {
        this.taskName = taskName;
        this.graphName = graph.getGraphName();
        this.uploader = graph.getUploader();
        this.totalWorkers = 0;
        this.taskStatus = "New";

        Map<Target.TargetPosition, Set<Target>> targetsPositions = graph.getTargetsByPositions();
        this.roots = targetsPositions.get(Target.TargetPosition.ROOT).size();
        this.middles = targetsPositions.get(Target.TargetPosition.MIDDLE).size();
        this.leaves = targetsPositions.get(Target.TargetPosition.LEAF).size();
        this.independents = targetsPositions.get(Target.TargetPosition.INDEPENDENT).size();
        this.targets = this.roots + this.middles + this.leaves + this.independents;

        Map<Graph.TaskType, Integer> taskPrices = graph.getTasksPricesMap();
        this.totalPayment = taskPrices.get(Graph.TaskType.Simulation) != null ?
                taskPrices.get(Graph.TaskType.Simulation) * this.targets : taskPrices.get(Graph.TaskType.Compilation) * this.targets;
    }

    public void addWorker() { this.totalWorkers++; }

    public void removeWorker() { this.totalWorkers--; }

    public void setTaskStatus(String status) { this.taskStatus = status; }

    public String getGraphName() {
        return this.graphName;
    }

    public String getUploader() {
        return this.uploader;
    }

    public Integer getRoots() {
        return this.roots;
    }

    public Integer getMiddles() {
        return this.middles;
    }

    public Integer getLeaves() {
        return this.leaves;
    }

    public Integer getIndependents() {
        return this.independents;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Integer getTargets() {
        return this.targets;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }

    public Integer getTotalWorkers() {
        return this.totalWorkers;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }
}
