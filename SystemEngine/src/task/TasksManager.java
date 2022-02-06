package task;

import dtos.DashboardTaskDetailsDTO;
import dtos.TaskCurrentInfoDTO;
import information.TaskTargetCurrentInfoTableItem;
import summaries.TargetSummary;
import target.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TasksManager {

    private static final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>();
    private static final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>();
    private static final Map<String, Set<String>> usersTasks = new HashMap<>();
    private static final Set<String> listOfAllTasks = new HashSet<>();
    private static final Map<String, DashboardTaskDetailsDTO> taskDetailsDTOMap = new HashMap<>();
    private static final Map<String, TaskCurrentInfoDTO> taskInfoMap = new HashMap<>();

    public synchronized boolean isTaskExists(String taskName) {
        return simulationTasksMap.containsKey(taskName.toLowerCase()) || compilationTasksMap.containsKey(taskName.toLowerCase());
    }

    public synchronized boolean isSimulationTask(String taskName) { return simulationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized CompilationTaskInformation getCompilationTaskInformation(String taskName) {
        return compilationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized SimulationTaskInformation getSimulationTaskInformation(String taskName) {
        return simulationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized void addSimulationTask(SimulationTaskInformation newTask) {
        simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }

    public synchronized void addCompilationTask(CompilationTaskInformation newTask) {
        compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }

    public synchronized void addUserTask(String taskCreator, String taskName) {
        if(!usersTasks.containsKey(taskCreator))
            usersTasks.put(taskCreator, new HashSet<>());

        usersTasks.get(taskCreator).add(taskName);
    }

    public synchronized Set<String> getAllTaskList()
    {
        return listOfAllTasks;
    }

    public synchronized Set<String> getUserTaskList(String userName)
    {
        return usersTasks.get(userName.toLowerCase());
    }

    public synchronized void addTaskDetailsDTO(String taskName, String creatorName, Graph graph)
    {
        taskDetailsDTOMap.put(taskName.toLowerCase(), new DashboardTaskDetailsDTO(taskName, creatorName, graph));
    }

    public synchronized DashboardTaskDetailsDTO getTaskDetailsDTO(String taskName)
    {
        return taskDetailsDTOMap.get(taskName.toLowerCase());
    }

    public synchronized void createNewTaskInfo(String taskName, Set<String> targets)
    {
        Set<TaskTargetCurrentInfoTableItem> infoSet = new HashSet<>();
        int i = 1;

        for(String curr : targets)
        {
            infoSet.add(new TaskTargetCurrentInfoTableItem(i++, curr,
                    TargetSummary.RuntimeStatus.Undefined.toString(),
                    TargetSummary.ResultStatus.Undefined.toString()));
        }

        TaskCurrentInfoDTO newInfo = new TaskCurrentInfoDTO("Not Started", infoSet, 0, null);
        taskInfoMap.put(taskName.toLowerCase(), newInfo);
    }

    public synchronized TaskCurrentInfoDTO getTaskCurrentInfo(String taskName)
    {
        return taskInfoMap.get(taskName.toLowerCase());
    }

    public synchronized void updateTargetInfoOnTask(String taskName, String targetName, TaskTargetCurrentInfoTableItem targetUpdate)
    {
        TaskCurrentInfoDTO currTargetInfo = taskInfoMap.get(taskName.toLowerCase());

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
//
//    private void updateTaskStatus(String taskName) {
//        boolean isTaskOver = true;
//
//        for(TaskTargetCurrentInfoTableItem curr : taskInfoMap.get(taskName).getTargetStatusSet())
//        {
//            if(curr.)
//        }
//    }
}
