package dtos;

import target.Graph;
import target.Target;

import java.util.Map;
import java.util.Set;

public class DashboardGraphDetailsDTO {
    private final String graphName;
    private final String uploader;
    private final Integer targets;
    private final Integer roots;
    private final Integer middles;
    private final Integer leaves;
    private final Integer independents;
    private Integer simulationPrice;
    private Integer compilationPrice;

    public DashboardGraphDetailsDTO(Graph graph) {
        this.graphName = graph.getGraphName();
        this.uploader = graph.getUploader();

        Map<Target.TargetPosition, Set<Target>> targetsPositions = graph.getTargetsByPositions();
        this.roots = targetsPositions.get(Target.TargetPosition.ROOT).size();
        this.middles = targetsPositions.get(Target.TargetPosition.MIDDLE).size();
        this.leaves = targetsPositions.get(Target.TargetPosition.LEAF).size();
        this.independents = targetsPositions.get(Target.TargetPosition.INDEPENDENT).size();
        this.targets = this.roots + this.middles + this.leaves + this.independents;

        Map<Graph.TaskType, Integer> taskPrices = graph.getTasksPricesMap();
        this.simulationPrice = taskPrices.get(Graph.TaskType.Simulation) != null ? taskPrices.get(Graph.TaskType.Simulation) : 0;
        this.compilationPrice = taskPrices.get(Graph.TaskType.Compilation) != null ? taskPrices.get(Graph.TaskType.Compilation) : 0;
    }

    public Integer getTargets() {
        return this.targets;
    }

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

    public Integer getSimulationPrice() {
        return this.simulationPrice;
    }

    public void setSimulationPrice(Integer simulationPrice) {
        this.simulationPrice = simulationPrice;
    }

    public Integer getCompilationPrice() {
        return this.compilationPrice;
    }

    public void setCompilationPrice(Integer compilationPrice) {
        this.compilationPrice = compilationPrice;
    }
}
