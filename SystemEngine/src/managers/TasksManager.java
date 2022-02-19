package managers;

import information.AllTaskDetails;
import information.WorkerTaskHistory;
import myExceptions.OpeningFileCrash;
import patterns.Patterns;
import summaries.GraphSummary;
import summaries.TargetSummary;
import tableItems.TaskTargetCurrentInfoTableItem;
import target.Graph;
import target.Target;
import task.CompilationTaskInformation;
import task.ExecutedTargetUpdates;
import task.SimulationTaskInformation;
import task.TaskThread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final Map<String, Map<String, WorkerTaskHistory>> workersTasksHistoryMap = new HashMap<>(); //Workers executedTargets (workerName, (taskName, info))
    private final Map<String, Set<String>> workerRegisteredTasksMap = new HashMap<>(); //Workers registered tasks (workerName, all tasks)
    private final Map<String, GraphSummary> graphSummaryMap = new HashMap<>(); //Graph summary of task (taskName, graph summary)
    private final Map<String, TaskThread> taskThreadMap = new HashMap<>(); //Task thread of task (taskName, task thread)

    //--------------------------------------------------- Formats ------------------------------------------------//
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

    //--------------------------------------------------- New Tasks ----------------------------------------------//
    public synchronized void addSimulationTask(SimulationTaskInformation newTask, GraphsManager graphsManager) throws OpeningFileCrash {
        this.simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator(), newTask.getTaskName());

        createNewAllTaskDetailsItem(newTask.getTaskName(), newTask.getGraphName(), newTask.getTaskCreator(),
                newTask.getTargetsToExecute(), "Simulation", graphsManager);

        createNewGraphSummary(newTask.getTaskName(), newTask.getGraphName(), graphsManager);

        createNewDirectoryOfTaskLogs(newTask.getTaskName());
    }

    public synchronized void addCompilationTask(CompilationTaskInformation newTask, GraphsManager graphsManager) throws OpeningFileCrash {
        this.compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        this.listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator(), newTask.getTaskName());

        createNewAllTaskDetailsItem(newTask.getTaskName(), newTask.getGraphName(), newTask.getTaskCreator(),
                newTask.getTargetsToExecute(), "Compilation", graphsManager);

        createNewGraphSummary(newTask.getTaskName(), newTask.getGraphName(), graphsManager);

        createNewDirectoryOfTaskLogs(newTask.getTaskName());
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
        addUserTask(creatorName, taskName);

        this.allTaskDetailsMap.put(taskName.toLowerCase(), new AllTaskDetails(taskName, taskName,
                creatorName, targets, infoSet, taskType, graphsManager.getGraph(graphName),
                "Task created by " + creatorName + " on " + this.formatter.format(new Date())));
    }
    //---------------------------------------------------- Admins -----------------------------------------------//
    public synchronized void addUserTask(String taskCreator, String taskName) {
        String taskCreatorLowerCase = taskCreator.toLowerCase();

        if(!this.adminsTasks.containsKey(taskCreatorLowerCase))
            this.adminsTasks.put(taskCreatorLowerCase, new HashSet<>());

        this.adminsTasks.get(taskCreatorLowerCase).add(taskName);
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

    public synchronized void removeWorkerRegistrationFromAllTasks(String workerName) {
        Set<String> registeredTasks = this.workerRegisteredTasksMap.get(workerName.toLowerCase());
        for(String currTask : registeredTasks)
            removeWorkerRegistrationFromTask(currTask, workerName);
    }

    public synchronized void addCreditsToWorker(String workerName, String taskName) {
        String workerNameLow = workerName.toLowerCase();
        String taskNameLow = taskName.toLowerCase();

        this.workersCredits.put(workerNameLow, this.workersCredits.get(workerNameLow) +
                this.allTaskDetailsMap.get(taskNameLow).getSinglePayment());

        this.workersTasksHistoryMap.get(workerNameLow).get(taskNameLow).newExecutedTarget();
    }

    public synchronized void addTargetToWorkerTaskHistory(String workerName, String taskName, String targetName) {
        this.workersTasksHistoryMap.get(workerName.toLowerCase()).get(taskName.toLowerCase()).newWorkedOnTarget(targetName);
    }

    public synchronized void addRegisteredTaskToWorker(String workerName, String taskName) {
        String workerNameLow = workerName.toLowerCase();
        String taskNameLow = taskName.toLowerCase();
        Integer payment = this.allTaskDetailsMap.get(taskNameLow).getSinglePayment();

        //Adding task to worker's registered tasks
        this.workerRegisteredTasksMap.computeIfAbsent(workerNameLow, k -> new HashSet<>());
        this.workerRegisteredTasksMap.get(workerNameLow).add(taskName);

        //Creating task history item for worker, if it wasn't created by now
        this.workersTasksHistoryMap.computeIfAbsent(workerNameLow, k -> new HashMap<>());
        this.workersTasksHistoryMap.get(workerNameLow).computeIfAbsent(taskNameLow, k -> new WorkerTaskHistory(payment));
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
        String taskNameLowerCase = taskName.toLowerCase();
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskNameLowerCase);
        TaskThread taskThread;
        String graphName = taskDetails.getGraphName();
        String originalTaskNameLowerCase = taskDetails.getOriginalTaskName().toLowerCase();

        taskDetails.setTaskStatus("Running");
        taskDetails.addToTaskLogHistory(userName + " started the task on " + this.formatter.format(new Date()) + "!");

        this.allTaskDetailsMap.get(taskNameLowerCase).setTaskStatus("Running");
        this.listOfActiveTasks.add(taskName);

        if(this.allTaskDetailsMap.get(taskNameLowerCase).getTaskType().equals("Simulation")) //Simulation task
            taskThread = new TaskThread(taskName, graphsManager.getGraph(graphName), taskDetails.getTargetsToExecute(),
                    this.graphSummaryMap.get(taskNameLowerCase), this.simulationTasksMap.get(originalTaskNameLowerCase),
                    null, false, taskDetails);
        else //Compilation task
            taskThread = new TaskThread(taskName, graphsManager.getGraph(graphName), taskDetails.getTargetsToExecute(),
                    this.graphSummaryMap.get(taskNameLowerCase),
                    null, this.compilationTasksMap.get(originalTaskNameLowerCase), false, taskDetails);

        taskThread.start();
        this.graphSummaryMap.get(taskNameLowerCase).startTheClock();
        this.taskThreadMap.put(taskNameLowerCase, taskThread);
    }

    public synchronized void pauseTask(String taskName, String adminName) {
        this.taskThreadMap.get(taskName.toLowerCase()).pauseTheTask();
        this.graphSummaryMap.get(taskName.toLowerCase()).pauseTheClock();

        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());

        taskDetails.setTaskStatus("Paused");
        taskDetails.addToTaskLogHistory(adminName + " paused the task on " + this.formatter.format(new Date()));
    }

    public synchronized void resumeTask(String taskName, String adminName) {
        this.taskThreadMap.get(taskName.toLowerCase()).continueTheTask();
        this.graphSummaryMap.get(taskName.toLowerCase()).continueTheClock();

        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());

        taskDetails.setTaskStatus("Running");
        taskDetails.addToTaskLogHistory(adminName + " resumed the task on " + this.formatter.format(new Date()));
    }

    public synchronized void stopTask(String taskName, String adminName) {
        this.taskThreadMap.get(taskName.toLowerCase()).stopTheTask();
        this.listOfActiveTasks.remove(taskName);
        this.workerRegisteredTasksMap.remove(taskName.toLowerCase());
        this.graphSummaryMap.get(taskName.toLowerCase()).stopTheClock();

        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());

        taskDetails.setTaskStatus("Stopped");
        taskDetails.addToTaskLogHistory(adminName + " stopped the task on " + this.formatter.format(new Date()));

        removeWorkersWithNoHistoryFromTask(taskName);
    }

    public synchronized void removeWorkersWithNoHistoryFromTask(String taskName) {
        String taskNameLow = taskName.toLowerCase();

        for(Map<String, WorkerTaskHistory> curr : this.workersTasksHistoryMap.values())
            if(curr.get(taskNameLow).getWorkingOnTargets().isEmpty())
                curr.remove(taskNameLow);
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
        taskDetails.addToTargetLogHistory(targetName, log);
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

    public void createNewDirectoryOfTaskLogs(String taskName) throws OpeningFileCrash {
        String directoryPath = Patterns.WORKING_DIRECTORY_PATH.toString() + "\\" + taskName + " - " + this.formatter.format(new Date());

        Path path = Paths.get(directoryPath);
        try {
            Files.createDirectories(path);
            this.allTaskDetailsMap.get(taskName.toLowerCase()).setDirectoryPath(directoryPath);
        } catch (IOException e) {
            throw new OpeningFileCrash(Paths.get(directoryPath));
        }
    }

    //----------------------------------------------------- Checks -----------------------------------------------//
    public synchronized boolean isTaskExists(String taskName) {
        return getAllTasksList().contains(taskName);
    }

    public synchronized boolean isSimulationTask(String taskName) { return this.simulationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized boolean isCompilationTask(String taskName) { return this.compilationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized boolean isTaskPausable(String taskName) {
        String taskStatus = this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus();
        return taskStatus.equalsIgnoreCase("Running");
    }

    public synchronized boolean isTaskResumable(String taskName) {
        String taskStatus = this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus();
        return taskStatus.equalsIgnoreCase("Paused");
    }

    public synchronized boolean isTaskStoppable(String taskName) {
        String taskStatus = this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus();
        return taskStatus.equalsIgnoreCase("Running") || taskStatus.equalsIgnoreCase("Paused");
    }

    public synchronized boolean isTaskRunning(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus().equalsIgnoreCase("Running");
    }

    public synchronized boolean isTaskFinished(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus().equalsIgnoreCase("Finished");
    }

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

    public synchronized AllTaskDetails getAllTaskDetails(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase());
    }

    public synchronized Integer getTaskFinishedTargets(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getFinishedTargets();
    }

    public synchronized Set<String> getWorkerRegisteredTasks(String workerName) {
        return this.workerRegisteredTasksMap.get(workerName.toLowerCase());
    }

    public synchronized Integer getWorkerTaskCredits(String workerName, String taskName)
    {
        WorkerTaskHistory taskHistory = this.workersTasksHistoryMap.get(workerName.toLowerCase()).get(taskName.toLowerCase());
        return taskHistory.getTotalCreditsFromTask();
    }

    public synchronized Integer getWorkerTotalCredits(String workerName) {
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

    public synchronized Map<String, WorkerTaskHistory> getWorkerTaskHistory(String workerName) {
        return this.workersTasksHistoryMap.get(workerName.toLowerCase());
    }

    public synchronized Set<String> getWorkerExecutedTargetsFromTask(String workerName, String taskName) {
        return getWorkerTaskHistory(workerName).get(taskName.toLowerCase()).getWorkingOnTargets();
    }

    public synchronized Integer getWorkerFinishedTargetsFromTask(String workerName, String taskName)
    {
        return getWorkerTaskHistory(workerName).get(taskName.toLowerCase()).getExecutedTargets();
    }

    public String getOriginalTaskName(String taskName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getOriginalTaskName();
    }

    public String getWorkerChosenTargetStatus(String taskName, String targetName) {
        TargetSummary targetSummary = this.graphSummaryMap.get(taskName.toLowerCase()).getTargetsSummaryMap().get(targetName);
        TargetSummary.ResultStatus resultStatus = targetSummary.getResultStatus();

        if(resultStatus.equals(TargetSummary.ResultStatus.Undefined)) //Target still running
            return "In process";
        else //Target finished
            return resultStatus.toString();
    }

    public GraphSummary getGraphSummary(String taskName)
    {
        return this.graphSummaryMap.get(taskName.toLowerCase());
    }

    //----------------------------------------------------- Methods -----------------------------------------------//
    public synchronized void removeTaskThread(String taskName) {
        this.taskThreadMap.remove(taskName.toLowerCase());
    }

    public synchronized void removeAllWorkersRegistrationsFromTask(String taskName) {
        for(String registeredWorker : this.allTaskDetailsMap.get(taskName.toLowerCase()).getRegisteredWorkers())
            this.workerRegisteredTasksMap.get(registeredWorker.toLowerCase()).remove(taskName);

        this.allTaskDetailsMap.get(taskName.toLowerCase()).removeAllWorkersRegistrationsFromTask();
    }

    public synchronized void removeTaskFromActiveList(String taskName) {
        this.listOfActiveTasks.remove(taskName);
        removeAllWorkersRegistrationsFromTask(taskName);
    }

    public synchronized String copyTask(String taskName, GraphsManager graphsManager, Boolean isIncremental) throws OpeningFileCrash {
        String taskNameLowerCase = taskName.toLowerCase();
        String copiedTaskName;
        AllTaskDetails originalTaskDetails = this.allTaskDetailsMap.get(taskNameLowerCase);
        Graph graph = graphsManager.getGraph(originalTaskDetails.getGraphName());

        AllTaskDetails copiedTaskDetails = originalTaskDetails.createCopy
                (graph, "A new copy of " + taskName + " was made by " + originalTaskDetails.getUploader() +
                        " on " + this.formatter.format(new Date()), isIncremental);

        copiedTaskName = copiedTaskDetails.getTaskName();

        while(this.allTaskDetailsMap.containsKey(copiedTaskName.toLowerCase()))
        {
            copiedTaskName = originalTaskDetails.generateNewTaskName();
            copiedTaskDetails.setTaskName(copiedTaskName);
        }

        this.allTaskDetailsMap.put(copiedTaskName.toLowerCase(), copiedTaskDetails);
        createNewDirectoryOfTaskLogs(copiedTaskName);

        createNewGraphSummary(copiedTaskName, copiedTaskDetails.getGraphName(), graphsManager);

        this.listOfAllTasks.add(copiedTaskName);
        addUserTask(copiedTaskDetails.getUploader(), copiedTaskName);

        return copiedTaskDetails.getTaskName();
    }

    public boolean isTaskRunBefore(String taskName) {
        String taskStatus = this.allTaskDetailsMap.get(taskName.toLowerCase()).getTaskStatus();
        return taskStatus.equalsIgnoreCase("Finished") || taskStatus.equalsIgnoreCase("Stopped");
    }

    public void workerAbortedTasks(String workerName) {
        String workerNameLowerCase = workerName.toLowerCase();
        String currTaskLowerCase;
        WorkerTaskHistory currTaskHistory;
        Set<String> abortedTargets;
        AllTaskDetails currTaskDetails;
        String addedLog;

        for(String currTask : this.workerRegisteredTasksMap.get(workerNameLowerCase)) //Scanning all worker's registered task, for targets he's working on
        {
            currTaskLowerCase = currTask.toLowerCase();
            currTaskHistory = this.workersTasksHistoryMap.get(workerNameLowerCase).get(currTaskLowerCase);
            abortedTargets = this.taskThreadMap.get(currTaskLowerCase).returnTargetsToWaitingList(currTaskHistory.getWorkingOnTargets());
            currTaskDetails = this.allTaskDetailsMap.get(currTaskLowerCase);

            for(String abortedTarget : abortedTargets) //Added abortion log for each aborted target
            {
                addedLog = "The worker " + workerName + " aborted the task on " + abortedTarget + " on " + this.formatter.format(new Date());
                currTaskDetails.addToTargetLogHistory(abortedTarget, addedLog);
                currTaskDetails.addToTaskLogHistory(addedLog);
            }

            removeWorkerRegistrationFromTask(currTask, workerName);
        }
    }

    public boolean isTargetExist(String taskName, String targetName) {
        return this.allTaskDetailsMap.get(taskName.toLowerCase()).getTargetsToExecute().contains(targetName);
    }

    public String getTargetRunningInfo(String taskName, Target target) {
        Set<String> runningTarget = this.allTaskDetailsMap.get(taskName.toLowerCase()).getTargetsToExecute();
        return this.graphSummaryMap.get(taskName.toLowerCase()).getRunningTargetInfo(target, runningTarget);
    }

    public boolean isIncrementalAnOption(String taskName) {

        return this.allTaskDetailsMap.get(taskName.toLowerCase()).isIncrementalAnOption();
    }

    public String getTaskSummary(String taskName) {
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());
        GraphSummary graphSummary = this.graphSummaryMap.get(taskName.toLowerCase());

        String summary = taskName + " summary:\n";
        graphSummary.calculateResults();
        Map<TargetSummary.ResultStatus, Integer> results = graphSummary.getAllResultStatus();

        summary += "Total time spent on task: " + graphSummary.getTime().toMillis() + "m/s\n";
        summary += "Number of targets succeeded: " + results.get(TargetSummary.ResultStatus.Success) + "\n";
        summary += "Number of targets succeeded with warnings: " + results.get(TargetSummary.ResultStatus.Warning) + "\n";
        summary += "Number of targets failed: " + results.get(TargetSummary.ResultStatus.Failure) + "\n";
        summary += "Number of targets skipped: " + graphSummary.getSkippedTargets() + "\n";

        for(TargetSummary currentTarget : graphSummary.getTargetsSummaryMap().values())
        {
            if(currentTarget.isRunning())
                summary += outputTargetTaskSummary(currentTarget.getTargetName(), graphSummary);
        }

        summary += "---------------------END OF SUMMARY---------------------\n";
        return summary;
    }

    public String outputTargetTaskSummary(String targetName, GraphSummary graphSummary) {
        TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
        String targetOutput = "-----------------------\n";

        targetOutput += "Target's name: " + targetName + "\n";
        targetOutput += "Target's result status: ";
        targetOutput += targetSummary.isSkipped() ?  "Skipped\n" : targetSummary.getResultStatus() + "\n";
        targetOutput += !targetSummary.isSkipped() ? "Target's running time: " + targetSummary.getTime().toMillis() + "m/s\n" : "";

        return targetOutput;
    }

    public void writeTargetSummaryToFile(String taskName, String targetName) {
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());
        String log = taskDetails.getTargetLogHistory(targetName);

        createLogFile(taskName, targetName, log);
    }

    public void writeTaskSummaryToFile(String taskName) {
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());
        String log = taskDetails.getTaskLogHistory();

        createLogFile(taskName, taskName + " Task Summary", log);
    }

    public void createLogFile(String taskName, String fileName, String log) {
        AllTaskDetails taskDetails = this.allTaskDetailsMap.get(taskName.toLowerCase());
        String directoryPath = taskDetails.getDirectoryPath();
        Path filePath = Paths.get(directoryPath + "\\" + fileName + ".log");

        try {
            Files.createFile(filePath);
            OutputStream targetFile = new FileOutputStream(filePath.toString(), true);
            targetFile.write(log.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}