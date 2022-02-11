package managers;

import information.AllTaskDetails;
import information.WorkerWorkOnTask;
import summaries.GraphSummary;
import summaries.TargetSummary;
import tableItems.TaskTargetCurrentInfoTableItem;
import target.Graph;
import target.Target;
import task.CompilationTaskInformation;
import task.ExecutedTargetUpdates;
import task.SimulationTaskInformation;
import task.TaskThread;

import java.text.SimpleDateFormat;
import java.util.*;

public class TasksManager {
    //--------------------------------------------------- Members ------------------------------------------------//
    private final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>(); //Static info about simulation task created (taskName, info)
    private final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>(); //Static info about compilation task created (taskName, info)
    private final Map<String, Set<String>> adminsTasks = new HashMap<>(); //Tasks of every admin (adminName, tasks made by him)
    private final Set<String> listOfAllTasks = new HashSet<>(); //Tasks names
    private final Set<String> listOfActiveTasks = new HashSet<>(); //Active tasks names (for workers)
    private final Map<String, AllTaskDetails> allTaskDetailsMap = new HashMap<>(); //Info about each task (taskName, info)
    private final Map<String, Integer> workersCredits = new HashMap<>(); //Workers' credit (name, credit)
    private final Map<String, Map<String, WorkerWorkOnTask>> workersWorksHistoryMap = new HashMap<>(); //Workers executedTargets (workerName, (taskName, info))
    private final Map<String, Set<String>> workerRegisteredTasksMap = new HashMap<>(); //Registered workers (taskName, all workers)
    private final Map<String, GraphSummary> graphSummaryMap = new HashMap<>(); //Graph summary of task (taskName, graph summary)
    private final Map<String, TaskThread> taskThreadMap = new HashMap<>(); //Task thread of task (taskName, task thread)

    //--------------------------------------------------- Formats ------------------------------------------------//
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

    //--------------------------------------------------- New Tasks ----------------------------------------------//
    public synchronized void addSimulationTask(SimulationTaskInformation newTask, GraphsManager graphsManager) {
        this.simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());

        createNewAllTaskDetailsItem(newTask.getTaskName(), newTask.getGraphName(), newTask.getTaskCreator(),
                newTask.getTargetsToExecute(), "Simulation", graphsManager);

        createNewGraphSummary(newTask.getTaskName(), newTask.getGraphName(), graphsManager);
    }

    public synchronized void addCompilationTask(CompilationTaskInformation newTask, GraphsManager graphsManager) {
        this.compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());

        createNewAllTaskDetailsItem(newTask.getTaskName(), newTask.getGraphName(), newTask.getTaskCreator(),
                newTask.getTargetsToExecute(), "Compilation", graphsManager);

        createNewGraphSummary(newTask.getTaskName(), newTask.getGraphName(), graphsManager);
    }

    private void createNewGraphSummary(String taskName, String graphName, GraphsManager graphsManager) {
        Graph graph = graphsManager.getGraph(graphName.toLowerCase());

        this.graphSummaryMap.put(taskName.toLowerCase(), new GraphSummary(graph));
    }

    private synchronized void createNewAllTaskDetailsItem(String taskName, String graphName, String creatorName,
                                                          Set<String> targets, String taskType, GraphsManager graphsManager) {
        Set<TaskTargetCurrentInfoTableItem> infoSet = new HashSet<>();
        int i = 1;

        for(String curr : targets)
        {
            infoSet.add(new TaskTargetCurrentInfoTableItem(i++, curr,
                    TargetSummary.RuntimeStatus.Undefined.toString(),
                    TargetSummary.ResultStatus.Undefined.toString()));
        }
        addUserTask(creatorName.toLowerCase(), taskName);

        this.allTaskDetailsMap.put(taskName.toLowerCase(), new AllTaskDetails(taskName,
                creatorName, targets, infoSet, taskType, graphsManager.getGraph(graphName),
                "Task created by " + creatorName + " on " + this.formatter.format(new Date())));
    }
    //---------------------------------------------------- Admins -----------------------------------------------//
    public synchronized void addUserTask(String taskCreator, String taskName) {
        if(!this.adminsTasks.containsKey(taskCreator))
            this.adminsTasks.put(taskCreator, new HashSet<>());

        this.adminsTasks.get(taskCreator).add(taskName);
    }

    //---------------------------------------------------- Workers ----------------------------------------------//
    public synchronized void registerWorkerToTask(String taskName, String workerName) {
        this.allTaskDetailsMap.get(taskName.toLowerCase()).addWorker(workerName);
        addRegisteredTaskToWorker(workerName, taskName);
    }

    public synchronized void removeWorkerRegistrationFromTask(String taskName, String workerName) {
        this.allTaskDetailsMap.get(taskName.toLowerCase()).removeWorker(workerName);
        removeRegisteredTaskFromWorker(workerName, taskName);
    }

    public synchronized void addCreditsToWorker(String workerName, String taskName) {
        this.workersCredits.put(workerName.toLowerCase(), this.workersCredits.get(workerName.toLowerCase()) +
                this.allTaskDetailsMap.get(taskName.toLowerCase()).getSinglePayment());
    }

    public synchronized void addRegisteredTaskToWorker(String workerName, String taskName) {
        //Creating new set of tasks if it's the first time the worker register
        if(this.workerRegisteredTasksMap.get(workerName.toLowerCase()) == null)
            this.workerRegisteredTasksMap.put(workerName.toLowerCase(), new HashSet<>());

        this.workerRegisteredTasksMap.get(workerName.toLowerCase()).add(taskName);
    }

    public synchronized void removeRegisteredTaskFromWorker(String workerName, String taskName) {
        this.workerRegisteredTasksMap.get(workerName.toLowerCase()).remove(taskName);
    }

    public synchronized boolean isWorkerRegisteredToTask(String workerName, String taskName) {
        return this.workerRegisteredTasksMap.containsKey(workerName.toLowerCase())
                && this.workerRegisteredTasksMap.get(workerName.toLowerCase()).contains(taskName);
    }

    //-------------------------------------------------- Update Tasks --------------------------------------------//
    public synchronized void startTask(String taskName, String userName, GraphsManager graphsManager) {
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());
        TaskThread taskThread;
        String graphName = taskDetails.getGraphName();

        taskDetails.setTaskStatus("Running");
        taskDetails.addToTaskLogHistory(userName + " started the task on " + this.formatter.format(new Date()) + "!");

        this.allTaskDetailsMap.get(taskName.toLowerCase()).setTaskStatus("Running");
        this.listOfActiveTasks.add(taskName);

        if(this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskType().equals("Simulation")) //Simulation task
            taskThread = new TaskThread(taskDetails.getTaskName(), graphsManager.getGraph(graphName),
                    this.graphSummaryMap.get(taskName.toLowerCase()), this.simulationTasksMap.get(taskName.toLowerCase()), null, false);
        else //Compilation task
            taskThread = new TaskThread(taskDetails.getTaskName(), graphsManager.getGraph(graphName), this.graphSummaryMap.get(taskName.toLowerCase()),
                    null, this.compilationTasksMap.get(taskName.toLowerCase()), false);

        taskThread.start();
        this.taskThreadMap.put(taskName.toLowerCase(), taskThread);
    }

    public synchronized void updateTargetInfoOnTask(ExecutedTargetUpdates updates, GraphsManager graphsManager) {
        String targetName = updates.getTargetName();
        String taskName = updates.getTaskName().toLowerCase();
        String runtimeStatus = updates.getRuntimeStatus();
        String resultStatus = updates.getResultStatus();
        String log = updates.getTaskLog();
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName);

        //Updating graph summary
        TargetSummary.RuntimeStatus runtime = convertRuntimeStatus(runtimeStatus);
        TargetSummary.ResultStatus result = convertResultStatus(resultStatus);
        String graphName = taskDetails.getGraphName();
        Target target = graphsManager.getGraph(graphName).getTarget(targetName);

        GraphSummary graphSummary = this.graphSummaryMap.get(taskName);
        graphSummary.UpdateTargetSummary(target, result, runtime);

        //Updating task details item
        taskDetails.updateInfo(graphSummary, log);
    }

    public TargetSummary.RuntimeStatus convertRuntimeStatus(String runtimeStatus) {
        if(runtimeStatus.equalsIgnoreCase("In process"))
            return TargetSummary.RuntimeStatus.InProcess;
        else if(runtimeStatus.equalsIgnoreCase("Finished"))
            return TargetSummary.RuntimeStatus.Finished;

        return TargetSummary.RuntimeStatus.Undefined;
    }

    public TargetSummary.ResultStatus convertResultStatus(String resultStatus) {
        if(resultStatus.equalsIgnoreCase("Success"))
            return TargetSummary.ResultStatus.Success;
        else if(resultStatus.equalsIgnoreCase("Warning"))
            return TargetSummary.ResultStatus.Warning;
        else if(resultStatus.equalsIgnoreCase("Failure"))
            return TargetSummary.ResultStatus.Failure;

        return TargetSummary.ResultStatus.Undefined;
    }

    //----------------------------------------------------- Checks -----------------------------------------------//
    public synchronized boolean isTaskExists(String taskName) {
        return this.simulationTasksMap.containsKey(taskName.toLowerCase()) || this.compilationTasksMap.containsKey(taskName.toLowerCase());
    }

    public synchronized boolean isSimulationTask(String taskName) { return this.simulationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized boolean isCompilationTask(String taskName) { return this.compilationTasksMap.containsKey(taskName.toLowerCase()); }

    //----------------------------------------------------- Getters -----------------------------------------------//
    public synchronized CompilationTaskInformation getCompilationTaskInformation(String taskName) {
        return this.compilationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized SimulationTaskInformation getSimulationTaskInformation(String taskName) {
        return this.simulationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized Set<String> getAllTasksList() {
        return this.listOfAllTasks;
    }

    public synchronized Set<String> getActiveTasksList() { return this.listOfActiveTasks; }

    public synchronized AllTaskDetails getTaskDetailsDTO(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase());
    }

    public synchronized Integer getTaskFinishedTargets(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getFinishedTargets();
    }

    public synchronized Set<String> getWorkerRegisteredTasks(String workerName) {
        return this.workerRegisteredTasksMap.get(workerName.toLowerCase());
    }

    public synchronized Integer getWorkerCredits(String workerName) {
        if(!this.workersCredits.containsKey(workerName.toLowerCase()))
            this.workersCredits.put(workerName.toLowerCase(), 0);

        return this.workersCredits.get(workerName.toLowerCase());
    }

    public synchronized Set<String> getUserTaskList(String userName) {
        return this.adminsTasks.get(userName.toLowerCase());
    }

    public synchronized TaskThread getTaskThread(String taskName) {
        return this.taskThreadMap.get(taskName.toLowerCase());
    }

    public synchronized boolean isTaskFinished(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus().equalsIgnoreCase("Finished");
    }

    public synchronized void removeTaskThread(String taskName) {
        this.taskThreadMap.remove(taskName.toLowerCase());
    }

    public synchronized void removeAllWorkersRegistrations(String taskName) {
        this.workerRegisteredTasksMap.remove(taskName);
    }

    public synchronized void removeTaskFromActiveList(String taskName) {
        this.listOfActiveTasks.remove(taskName);
        removeAllWorkersRegistrations(taskName);
    }
}