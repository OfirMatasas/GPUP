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

    private final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>();
    private final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>();
    private final Map<String, Set<String>> usersTasks = new HashMap<>();
    private final Set<String> listOfAllTasks = new HashSet<>();
    private final Map<String, DashboardTaskDetailsDTO> taskDetailsDTOMap = new HashMap<>();
    private final Map<String, TaskCurrentInfoDTO> taskInfoMap = new HashMap<>();
    private final Map<String, Integer> workersCredits = new HashMap<>();
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
        if(!this.usersTasks.containsKey(taskCreator))
            this.usersTasks.put(taskCreator, new HashSet<>());

        this.usersTasks.get(taskCreator).add(taskName);
    }

    public synchronized void registerWorkerToTask(String taskName, String workerName) {
        this.taskDetailsDTOMap.get(taskName.toLowerCase()).addWorker(workerName);
        this.taskInfoMap.get(taskName.toLowerCase()).workerRegisteredToTask();
    }

    public synchronized void removeWorkerRegistrationFromTask(String taskName, String workerName) {
        this.taskDetailsDTOMap.get(taskName.toLowerCase()).removeWorker(workerName);
        this.taskInfoMap.get(taskName.toLowerCase()).workerLeftTask();
    }

    public synchronized Set<String> getAllTaskList()
    {
        return this.listOfAllTasks;
    }

    public synchronized Set<String> getUserTaskList(String userName) {
        return this.usersTasks.get(userName.toLowerCase());
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

    public synchronized void updateTargetInfoOnTask(String taskName, String targetName, TaskTargetCurrentInfoTableItem targetUpdate) {
        TaskCurrentInfoDTO currTargetInfo = this.taskInfoMap.get(taskName.toLowerCase());

        for(TaskTargetCurrentInfoTableItem curr : currTargetInfo.getTargetStatusSet())
        {
            if(curr.getTargetName().equalsIgnoreCase(targetName))
            {
                curr.updateItem(targetUpdate);
                break;
            }
        }

//        updateTaskStatus(taskName);
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
