package dtos;

import target.Graph;
import target.Target;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DashboardTaskDetailsDTO {

    private final String taskType;
    private final String taskName;
    private final String graphName;
    private final String uploader;
    private final Integer targets;
    private Integer roots = 0;
    private Integer middles = 0;
    private Integer leaves = 0;
    private Integer independents = 0;
    private final Integer totalPayment;
    private final Set<String> registeredWorkers;
    private String taskStatus;

    public DashboardTaskDetailsDTO(String taskName, String creatorName, Set<String> targetsToExecute, String taskType, Graph graph) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.graphName = graph.getGraphName();
        this.uploader = creatorName;
        this.registeredWorkers = new HashSet<>();
        this.taskStatus = "New";

        if(targetsToExecute.size() == graph.getGraphTargets().size())
        {
            Map<Target.TargetPosition, Set<Target>> targetsPositions = graph.getTargetsByPositions();

            this.roots = targetsPositions.get(Target.TargetPosition.ROOT).size();
            this.middles = targetsPositions.get(Target.TargetPosition.MIDDLE).size();
            this.leaves = targetsPositions.get(Target.TargetPosition.LEAF).size();
            this.independents = targetsPositions.get(Target.TargetPosition.INDEPENDENT).size();
        }
        else
        {
            Target.TargetPosition currTargetPosition;

            for(String currTargetName : targetsToExecute)
            {
                currTargetPosition = graph.getTarget(currTargetName).getTargetPosition();

                if(currTargetPosition.equals(Target.TargetPosition.ROOT))
                    ++this.roots;
                else if(currTargetPosition.equals(Target.TargetPosition.MIDDLE))
                    ++this.middles;
                else if(currTargetPosition.equals(Target.TargetPosition.LEAF))
                    ++this.leaves;
                else
                    ++this.independents;
            }
        }

        this.targets = this.roots + this.middles + this.leaves + this.independents;

        Map<Graph.TaskType, Integer> taskPrices = graph.getTasksPricesMap();
        this.totalPayment = taskPrices.get(Graph.TaskType.Simulation) != null ?
                taskPrices.get(Graph.TaskType.Simulation) * this.targets : taskPrices.get(Graph.TaskType.Compilation) * this.targets;
    }

    public void addWorker(String workerName) {
        this.registeredWorkers.add(workerName);
    }

    public void removeWorker(String workerName) {
        this.registeredWorkers.remove(workerName);
    }

    public void setTaskStatus(String status) { this.taskStatus = status; }

    public String getTaskType() {
        return this.taskType;
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

    public String getTaskName() {
        return this.taskName;
    }

    public Integer getTargets() {
        return this.targets;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }

    public Set<String> getRegisteredWorkers() {
        return this.registeredWorkers;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }
}
