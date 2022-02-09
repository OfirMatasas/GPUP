package managers;

import dtos.DashboardTaskDetailsDTO;
import dtos.TaskCurrentInfoDTO;
import tableItems.TaskTargetCurrentInfoTableItem;
import summaries.TargetSummary;
import target.Graph;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;

import java.text.SimpleDateFormat;
import java.util.*;

public class TasksManager {

    private final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>(); //Static info
    private final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>(); //Static info
    private final Map<String, Set<String>> adminsTasks = new HashMap<>(); //Holds all tasks of every admin
    private final Set<String> listOfAllTasks = new HashSet<>(); //Holds all tasks names
    private final Set<String> listOfActiveTasks = new HashSet<>(); //Holds all active tasks names (for workers)
    private final Map<String, DashboardTaskDetailsDTO> taskDetailsDTOMap = new HashMap<>();
    private final Map<String, TaskCurrentInfoDTO> taskInfoMap = new HashMap<>();
    private final Map<String, Integer> workersCredits = new HashMap<>();
    private final Map<String, Set<String>> workerRegisteredTasksMap = new HashMap<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

    public synchronized boolean isTaskExists(String taskName) {
        return this.simulationTasksMap.containsKey(taskName.toLowerCase()) || this.compilationTasksMap.containsKey(taskName.toLowerCase());
    }

    public synchronized boolean isSimulationTask(String taskName) { return this.simulationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized CompilationTaskInformation getCompilationTaskInformation(String taskName) {
        return this.compilationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized SimulationTaskInformation getSimulationTaskInformation(String taskName) {
        return this.simulationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized void addSimulationTask(SimulationTaskInformation newTask) {
        this.simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
        createNewTaskInfo(newTask.getTaskName(), newTask.getTaskCreator(), newTask.getTargetsToExecute());
    }

    public synchronized void addCompilationTask(CompilationTaskInformation newTask) {
        this.compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
        createNewTaskInfo(newTask.getTaskName(), newTask.getTaskCreator(), newTask.getTargetsToExecute());
    }

    public synchronized void addUserTask(String taskCreator, String taskName) {
        if(!this.adminsTasks.containsKey(taskCreator))
            this.adminsTasks.put(taskCreator, new HashSet<>());

        this.adminsTasks.get(taskCreator).add(taskName);
    }

    public synchronized void registerWorkerToTask(String taskName, String workerName) {
        this.taskDetailsDTOMap.get(taskName.toLowerCase()).addWorker(workerName);
        this.taskInfoMap.get(taskName.toLowerCase()).workerRegisteredToTask();
        addRegisteredTaskToWorker(workerName, taskName);
    }

    public synchronized void removeWorkerRegistrationFromTask(String taskName, String workerName) {
        this.taskDetailsDTOMap.get(taskName.toLowerCase()).removeWorker(workerName);
        this.taskInfoMap.get(taskName.toLowerCase()).workerLeftTask();
        removeRegisteredTaskFromWorker(workerName, taskName);
    }

    public synchronized Set<String> getAllTasksList()
    {
        return this.listOfAllTasks;
    }

    public synchronized Set<String> getActiveTasksList() { return this.listOfActiveTasks; }

    public synchronized Set<String> getUserTaskList(String userName) {
        return this.adminsTasks.get(userName.toLowerCase());
    }

    public synchronized void addTaskDetailsDTO(String taskName, String creatorName, String taskType, Set<String> targetsToExecute, Graph graph) {
        this.taskDetailsDTOMap.put(taskName.toLowerCase(), new DashboardTaskDetailsDTO(taskName, creatorName, targetsToExecute, taskType, graph));
    }

    public synchronized DashboardTaskDetailsDTO getTaskDetailsDTO(String taskName) {
        return this.taskDetailsDTOMap.get(taskName.toLowerCase());
    }

    public synchronized void createNewTaskInfo(String taskName, String taskCreator, Set<String> targets) {
        Set<TaskTargetCurrentInfoTableItem> infoSet = new HashSet<>();
        int i = 1;

        for(String curr : targets)
        {
            infoSet.add(new TaskTargetCurrentInfoTableItem(i++, curr,
                    TargetSummary.RuntimeStatus.Undefined.toString(),
                    TargetSummary.ResultStatus.Undefined.toString()));
        }

        TaskCurrentInfoDTO newInfo = new TaskCurrentInfoDTO("Not Started", infoSet, 0,
                "Task created by " + taskCreator + " on " + this.formatter.format(new Date()));

        this.taskInfoMap.put(taskName.toLowerCase(), newInfo);
    }

    public synchronized TaskCurrentInfoDTO getTaskCurrentInfo(String taskName) {
        return this.taskInfoMap.get(taskName.toLowerCase());
    }

    public synchronized void addCreditsToWorker(String workerName, String taskName) {
        this.workersCredits.put(workerName, this.workersCredits.get(workerName) +
                this.taskDetailsDTOMap.get(taskName.toLowerCase()).getSinglePayment());
    }

    public synchronized void updateTargetInfoOnTask(String taskName, String targetName, String runtimeStatus, String resultStatus) {
        this.taskInfoMap.get(taskName.toLowerCase()).updateTargetStatus(targetName, runtimeStatus, resultStatus);
    }

    public synchronized Integer getTaskFinishedTargets(String taskName) {
        return this.taskInfoMap.get(taskName.toLowerCase()).getFinishedTargets();
    }

    public synchronized Set<String> getWorkerRegisteredTasks(String workerName) {
        return this.workerRegisteredTasksMap.get(workerName.toLowerCase());
    }

    public synchronized Integer getWorkerCredits(String workerName) {
        if(!this.workersCredits.containsKey(workerName.toLowerCase()))
            this.workersCredits.put(workerName.toLowerCase(), 0);

        return this.workersCredits.get(workerName.toLowerCase());
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

    public void startTask(String taskName, String userName) {

        TaskCurrentInfoDTO taskInfo = this.taskInfoMap.get(taskName.toLowerCase());

        taskInfo.changeTaskStatus("Started");
        taskInfo.addToLogHistory(userName + " started the task on " + this.formatter.format(new Date()) + "!");

        this.listOfActiveTasks.add(taskName);
    }

//    private void updateTaskStatus(String taskName) {
//        boolean isTaskOver = true;
//
//        for(TaskTargetCurrentInfoTableItem curr : taskInfoMap.get(taskName).getTargetStatusSet())
//        {
//            if(curr.)
//        }
//    }
}
