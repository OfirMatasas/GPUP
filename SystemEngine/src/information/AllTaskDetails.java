package information;

import summaries.GraphSummary;
import summaries.TargetSummary;
import tableItems.TaskTargetCurrentInfoTableItem;
import target.Graph;
import target.Target;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllTaskDetails {
    private final String taskType;
    private final String taskName;
    private final String graphName;
    private final String uploader;
    private String taskStatus;
    private final Integer targets;
    private Integer roots = 0;
    private Integer middles = 0;
    private Integer leaves = 0;
    private Integer independents = 0;
    private final Integer singlePayment;
    private final Integer totalPayment;
    private final Set<String> registeredWorkers;
    private final Set<TaskTargetCurrentInfoTableItem> targetStatusSet;
    private String logHistory;
    private Integer finishedTargets;

    public AllTaskDetails(String taskName, String creatorName, Set<String> targetsToExecute,
                          Set<TaskTargetCurrentInfoTableItem> targetStatusSet, String taskType,
                          Graph graph, String logHistory) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.graphName = graph.getGraphName();
        this.uploader = creatorName;
        this.registeredWorkers = new HashSet<>();
        this.taskStatus = "New";
        this.finishedTargets = 0;
        this.logHistory = logHistory + "\n\n";
        this.targetStatusSet = targetStatusSet;

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
        this.singlePayment = taskType.equals(Graph.TaskType.Simulation.toString()) ?
                taskPrices.get(Graph.TaskType.Simulation) : taskPrices.get(Graph.TaskType.Compilation);
        this.totalPayment = this.singlePayment * this.targets;
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

    public Integer getSinglePayment() {
        return this.singlePayment;
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

    public Set<TaskTargetCurrentInfoTableItem> getTargetStatusSet() {
        return this.targetStatusSet;
    }

    public Integer getRegisteredWorkersNumber() {
        return this.registeredWorkers.size();
    }

    public String getLogHistory() {
        return this.logHistory;
    }

    public Integer getFinishedTargets() {
        return this.finishedTargets;
    }

    public synchronized void updateInfo(GraphSummary graphSummary, String log)
    {
        TargetSummary targetSummary;
        String runtimeStatus, resultStatus;
        this.finishedTargets = 0;

        for(TaskTargetCurrentInfoTableItem curr : this.targetStatusSet)
        {
            targetSummary = graphSummary.getTargetsSummaryMap().get(curr.getTargetName());
            runtimeStatus = targetSummary.getRuntimeStatus().toString();
            resultStatus = targetSummary.getResultStatus().toString();

            curr.updateItem(runtimeStatus, resultStatus);

            if(runtimeStatus.equalsIgnoreCase("Finished") || runtimeStatus.equalsIgnoreCase("Skipped"))
                ++this.finishedTargets;
        }

        this.logHistory += log + "\n\n";

        if(this.finishedTargets == this.targetStatusSet.size())
            this.taskStatus = "Finished";
    }

    public void addToTaskLogHistory(String addedInfo) { this.logHistory += addedInfo + "\n\n"; }
}