package information;

import summaries.GraphSummary;
import summaries.TargetSummary;
import tableItems.TaskTargetCurrentInfoTableItem;
import target.Graph;
import target.Target;

import java.util.*;

public class AllTaskDetails {
    //------------------------------------------------ Members ---------------------------------------------------//
    private final String taskType;
    private final String taskName;
    private final String originalTaskName;
    private Integer copies;
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
    private String taskLogHistory;
    private final Map<String, String> targetLogHistory; //(target, log)
    private Integer finishedTargets;
    private final Set<String> targetsToExecute;

    //--------------------------------------------- Constructors ------------------------------------------------//
    public AllTaskDetails(String taskName, String originalTaskName, String creatorName, Set<String> targetsToExecute,
                          Set<TaskTargetCurrentInfoTableItem> targetStatusSet, String taskType,
                          Graph graph, String log) {
        this.taskName = taskName;
        this.originalTaskName = originalTaskName;
        this.taskType = taskType;
        this.graphName = graph.getGraphName();
        this.uploader = creatorName;
        this.registeredWorkers = new HashSet<>();
        this.taskStatus = "New";
        this.finishedTargets = 0;
        this.taskLogHistory = log + "\n\n";
        this.targetStatusSet = targetStatusSet;
        this.targetLogHistory = new HashMap<>();
        this.copies = 0;
        this.targetsToExecute = new HashSet<>(targetsToExecute);

        for (String currTarget : targetsToExecute)
            this.targetLogHistory.put(currTarget.toLowerCase(), "");

        if (targetsToExecute.size() == graph.getGraphTargets().size()) {
            Map<Target.TargetPosition, Set<Target>> targetsPositions = graph.getTargetsByPositions();

            this.roots = targetsPositions.get(Target.TargetPosition.ROOT).size();
            this.middles = targetsPositions.get(Target.TargetPosition.MIDDLE).size();
            this.leaves = targetsPositions.get(Target.TargetPosition.LEAF).size();
            this.independents = targetsPositions.get(Target.TargetPosition.INDEPENDENT).size();
        } else {
            Target.TargetPosition currTargetPosition;

            for (String currTargetName : targetsToExecute) {
                currTargetPosition = graph.getTarget(currTargetName).getTargetPosition();

                if (currTargetPosition.equals(Target.TargetPosition.ROOT))
                    ++this.roots;
                else if (currTargetPosition.equals(Target.TargetPosition.MIDDLE))
                    ++this.middles;
                else if (currTargetPosition.equals(Target.TargetPosition.LEAF))
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

    public synchronized AllTaskDetails createCopy(Graph graph, String log, Boolean isIncremental) {
        String copiedTaskName = this.taskName + ++this.copies;
        Set<TaskTargetCurrentInfoTableItem> copiedTargetsStatusSet = new HashSet<>();
        int i = 1;

        for(TaskTargetCurrentInfoTableItem curr : this.targetStatusSet)
        {
            if(!isIncremental) //Copy all set as is, just reset result and runtime statuses
                copiedTargetsStatusSet.add(new TaskTargetCurrentInfoTableItem(curr.getTargetNumber(), curr.getTargetName(), "Undefined", "Undefined"));
            else if(!curr.getResultStatus().equalsIgnoreCase("Success")
                    && !curr.getResultStatus().equalsIgnoreCase("Warning")) //Incremental - copy only those who failed the last run
                copiedTargetsStatusSet.add(new TaskTargetCurrentInfoTableItem(i++, curr.getTargetName(), "Undefined", "Undefined"));
        }

        Set<String> copiedTargetsToExecute = new HashSet<>();
        for(TaskTargetCurrentInfoTableItem curr : copiedTargetsStatusSet)
            copiedTargetsToExecute.add(curr.getTargetName());

        return new AllTaskDetails(copiedTaskName, this.originalTaskName, this.uploader, copiedTargetsToExecute,
                copiedTargetsStatusSet, this.taskType, graph, log);
    }

    //------------------------------------------------ Getters ---------------------------------------------------//
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

    public String getTaskLogHistory() {
        return this.taskLogHistory;
    }

    public Integer getFinishedTargets() {
        return this.finishedTargets;
    }

    public String getOriginalTaskName(){ return this.originalTaskName; }

    public String getTargetLogHistory(String targetName) {
        return this.targetLogHistory.get(targetName.toLowerCase());
    }

    public Set<String> getTargetsToExecute() {
        return this.targetsToExecute;
    }

    //------------------------------------------------ Setters ---------------------------------------------------//
    public void setTaskStatus(String status) {
        this.taskStatus = status;
    }

    //------------------------------------------------ Methods ---------------------------------------------------//
    public synchronized void updateInfo(GraphSummary graphSummary, String log) {
        TargetSummary targetSummary;
        String runtimeStatus, resultStatus;
        this.finishedTargets = 0;

        for (TaskTargetCurrentInfoTableItem curr : this.targetStatusSet) {
            targetSummary = graphSummary.getTargetsSummaryMap().get(curr.getTargetName());

            if(targetSummary.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.InProcess))
                runtimeStatus = "In process";
            else
                runtimeStatus = targetSummary.getRuntimeStatus().toString();

            resultStatus = targetSummary.getResultStatus().toString();

            curr.updateItem(runtimeStatus, resultStatus);

            if (runtimeStatus.equalsIgnoreCase("Finished") || runtimeStatus.equalsIgnoreCase("Skipped"))
                ++this.finishedTargets;
        }

        this.taskLogHistory += log + "\n\n";

        if (this.finishedTargets == this.targetStatusSet.size())
            this.taskStatus = "Finished";
    }

    public void addWorker(String workerName) {
        this.registeredWorkers.add(workerName);
    }

    public void removeWorker(String workerName) {
        this.registeredWorkers.remove(workerName);
    }

    public void removeAllWorkersRegistrationsFromTask() { this.registeredWorkers.clear(); }

    public void addToTaskLogHistory(String addedInfo) {
        this.taskLogHistory += addedInfo + "\n\n";
    }

    public void addToTargetLogHistory(String targetName, String addedInfo) {
        this.targetLogHistory.put(targetName.toLowerCase(), this.targetLogHistory.get(targetName.toLowerCase()) + addedInfo + "\n");
    }

    public void updateTargetRuntimeStatus(String targetName, String runtimeStatus) {
        this.targetStatusSet.stream().
                filter(p -> p.getTargetName().equalsIgnoreCase(targetName)).
                findFirst().ifPresent(p -> p.setRuntimeStatus(runtimeStatus));
    }

    public boolean isIncrementalAnOption()
    {
        return this.targetStatusSet.stream()
                .anyMatch(p-> p.getResultStatus().equalsIgnoreCase("Undefined") || p.getResultStatus().equalsIgnoreCase("Failure"));
    }
}