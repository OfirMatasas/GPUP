package servlets.task;

import com.google.gson.Gson;
import dtos.WorkerChosenTargetDTO;
import dtos.WorkerChosenTaskDTO;
import information.AllTaskDetails;
import information.WorkerTaskHistory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.GraphsManager;
import managers.TasksManager;
import managers.UserManager;
import myExceptions.OpeningFileCrash;
import tableItems.WorkerChosenTargetInformationTableItem;
import tableItems.WorkerChosenTaskInformationTableItem;
import target.Target;
import task.ExecutedTargetUpdates;
import utils.ServletUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "TaskUpdateServlet", urlPatterns = "/task/update")
public class TaskUpdateServlet extends HttpServlet {

    //------------------------------------------------- Get -------------------------------------------------//
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if(req.getParameter("task-update") != null) //Returning task current information to admin
            returnTaskCurrentInfoToAdmin(req, resp, tasksManager);
        else if(req.getParameter("registered-tasks") != null) //Returning worker's registered tasks
            returnWorkerRegisteredTasks(req, resp, tasksManager, userManager);
        else if(req.getParameter("executed-targets") != null) //Returning worker's executed targets
            returnWorkerExecutedTargets(req, resp, tasksManager, userManager);
        else if(req.getParameter("credits") != null) //Returning worker's credits
            returnWorkerCurrentCredits(req, resp, tasksManager, userManager);
        else if(req.getParameter("chosen-task") != null)
            returnWorkerChosenTask(req, resp, tasksManager, userManager);
        else if(req.getParameter("chosen-target") != null)
            returnWorkerChosenTarget(req, resp, tasksManager, userManager);
        else if(req.getParameter("admin-target-info") != null)
            returnAdminTargetInfo(req, resp, tasksManager);
        else if(req.getParameter("task-log") != null)
            returnAdminTaskLog(req, resp, tasksManager);
        else //Invalid request
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnAdminTaskLog(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        String taskName = req.getParameter("task-log");
        String log;

        if(tasksManager.isTaskExists(taskName)) //Valid task name
        {
            log = tasksManager.getAllTaskDetails(taskName).getTaskLogHistory();
            resp.getWriter().write(new Gson().toJson(log, String.class));
            responseMessageAndCode(resp, taskName + " log pulled successfully!", HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid task name
            responseMessageAndCode(resp, taskName + " not exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnAdminTargetInfo(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        String targetName = req.getParameter("admin-target-info");
        String taskName = req.getParameter("task");

        if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else if(targetName == null || !tasksManager.isTargetExist(taskName, targetName)) //Invalid target name
            responseMessageAndCode(resp, "Invalid target name!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid request
        {
            String graphName = tasksManager.getAllTaskDetails(taskName).getGraphName();
            Target target = ServletUtils.getGraphsManager(getServletContext()).getGraph(graphName).getTarget(targetName);

            String targetInfo = tasksManager.getTargetRunningInfo(taskName, target);
            resp.getWriter().write(new Gson().toJson(targetInfo, String.class));
            responseMessageAndCode(resp, "Valid request for target running information!", HttpServletResponse.SC_ACCEPTED);
        }
    }

    private void returnWorkerExecutedTargets(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String workerName = req.getParameter("executed-targets");

        if(userManager.isUserExists(workerName)) //Valid worker name
        {
            Map<String, WorkerTaskHistory> workerTasksHistory = tasksManager.getWorkerTaskHistory(workerName);

            if(workerTasksHistory != null)
            {
                Set<String> executedTargets = new HashSet<>();
                for(String currTask : workerTasksHistory.keySet())
                    for(String currTarget : workerTasksHistory.get(currTask).getTargets())
                        executedTargets.add(currTask + " - " + currTarget);

                resp.getWriter().write(new Gson().toJson(executedTargets, Set.class));
                responseMessageAndCode(resp, "Successfully pulled worker's executed targets!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "The worker " + workerName + " is not registered to any task!", HttpServletResponse.SC_BAD_REQUEST);
        }
        else //Worker not exists
            responseMessageAndCode(resp, "The worker " + workerName + " not exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnWorkerChosenTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String workerName = req.getParameter("username");
        String taskName = req.getParameter("chosen-task");

        if(workerName == null || !userManager.isUserExists(workerName))
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else if(taskName == null || !tasksManager.isTaskExists(taskName))
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid request
        {
            AllTaskDetails currInfo = tasksManager.getAllTaskDetails(taskName);
            Integer executedTargets = tasksManager.getWorkerExecutedTargetsFromTask(workerName, taskName).size();

            WorkerChosenTaskInformationTableItem tableItem = new WorkerChosenTaskInformationTableItem(taskName,
                    currInfo.getTaskStatus(), currInfo.getRegisteredWorkersNumber(), executedTargets , tasksManager.getWorkerCredits(workerName));

            WorkerChosenTaskDTO returnedDTO = new WorkerChosenTaskDTO(tableItem, currInfo.getTargetStatusSet().size(), currInfo.getFinishedTargets());

            resp.getWriter().write(new Gson().toJson(returnedDTO, WorkerChosenTaskDTO.class));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    private void returnWorkerChosenTarget(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String workerName = req.getParameter("username");
        String taskName = getTaskNameFromWorkerChosenTargetRequest(req.getParameter("chosen-target"));
        String targetName = getTargetNameFromWorkerChosenTargetRequest(req.getParameter("chosen-target"));

        if(workerName == null || !userManager.isUserExists(workerName))
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else if(taskName == null || !tasksManager.isTaskExists(taskName))
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else if(targetName == null)
            responseMessageAndCode(resp, "Invalid target name!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid request
        {
            AllTaskDetails currInfo = tasksManager.getAllTaskDetails(taskName);
            String targetStatus = tasksManager.getWorkerChosenTargetStatus(taskName, targetName);

            WorkerChosenTargetInformationTableItem tableItem = new WorkerChosenTargetInformationTableItem(targetName,
                    taskName, currInfo.getTaskType(), targetStatus, currInfo.getSinglePayment());
            String log = currInfo.getTargetLogHistory(targetName);
            WorkerChosenTargetDTO dto = new WorkerChosenTargetDTO(tableItem, log);

            resp.getWriter().write(new Gson().toJson(dto, WorkerChosenTargetDTO.class));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    private String getTaskNameFromWorkerChosenTargetRequest(String fullName) {
        int index = fullName.indexOf(" - ");

        if(index != -1)
        {
            TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
            String taskNameFromChosenTarget =  fullName.substring(0, index);
            AllTaskDetails taskDetails = tasksManager.getAllTaskDetails(taskNameFromChosenTarget);

            if(taskDetails != null)
                return taskDetails.getTaskName();
            return null;
        }
        return null;
    }

    private String getTargetNameFromWorkerChosenTargetRequest(String fullName) {
        int index = fullName.indexOf(" - ");

        if(index != -1)
            return fullName.substring(index + 3).trim();
        return null;
    }

    private void returnWorkerCurrentCredits(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) {
        String workerName = req.getParameter("credits");

        if(workerName != null && userManager.isUserExists(workerName)) //Valid credit request
        {
            resp.addHeader("credits", tasksManager.getWorkerCredits(workerName).toString());
            responseMessageAndCode(resp, "Successfully pulled worker's credits!", HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid request
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnWorkerRegisteredTasks(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String workerName = req.getParameter("registered-tasks");

        if(workerName != null && userManager.isUserExists(workerName)) //Worker exists in the system
        {
            Set<String> registeredTasks = tasksManager.getWorkerRegisteredTasks(workerName);
            String registeredTasksAsString = new Gson().toJson(registeredTasks, Set.class);
            resp.getWriter().write(registeredTasksAsString);

            responseMessageAndCode(resp, "Successfully pulled worker's registered tasks!", HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid worker name
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnTaskCurrentInfoToAdmin(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        String taskName = req.getParameter("task-update");
        Gson gson = new Gson();

        if(tasksManager.isTaskExists(taskName)) //Task exists
        {
            AllTaskDetails updatedInfo = tasksManager.getAllTaskDetails(taskName);
            String infoAsString = gson.toJson(updatedInfo, AllTaskDetails.class);
            resp.getWriter().write(infoAsString);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else //Task not exists
            responseMessageAndCode(resp, "The task " + taskName + " doesn't exist in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    //------------------------------------------------- Post -------------------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("start-task") != null) //Requesting to start a task
        {
            try {
                startTask(req, resp, tasksManager);
            } catch (OpeningFileCrash e) {
                e.printStackTrace();
            }
        }
        else if(req.getParameter("pause-task") != null)
            pauseTask(req, resp, tasksManager);
        else if(req.getParameter("resume-task") != null)
            resumeTask(req, resp, tasksManager);
        else if(req.getParameter("stop-task") != null)
            stopTask(req, resp, tasksManager);
        else if(req.getHeader("executed-task-update") != null)
            updateTargetOnTask(req, resp, tasksManager);
        else
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private synchronized void startTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws OpeningFileCrash {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String taskName = req.getParameter("start-task");
        String userName = req.getParameter("username");

        if(!tasksManager.isTaskExists(taskName)) //Invalid task name
            responseMessageAndCode(resp, "The task " + taskName + " doesn't exist in the system!", HttpServletResponse.SC_BAD_REQUEST);
        else if(tasksManager.isTaskRunning(taskName))
            responseMessageAndCode(resp, "The task " + taskName + " is already running!", HttpServletResponse.SC_BAD_REQUEST);
        else if(userName == null || !userManager.isUserExists(userName)) //Invalid username
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else if(!userManager.isAdmin(userName)) //Invalid access
            responseMessageAndCode(resp, "Only admins can run tasks!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid Parameters for running task
        {
            GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());
            boolean runBefore = tasksManager.isTaskRunBefore(taskName);

            if(req.getParameter("incremental") != null) //Incremental requested
            {
                if(!tasksManager.isIncrementalAnOption(taskName)) //No failed targets
                    responseMessageAndCode(resp, "The task " + taskName + " cannot run incrementally for no failed targets!", HttpServletResponse.SC_BAD_REQUEST);
                else if(runBefore) //Valid incremental request - creating a copy of the task and run incrementally
                {
                    String copiedTaskName = tasksManager.copyTask(taskName, graphsManager, true);
                    tasksManager.startTask(copiedTaskName, userName, graphsManager);
                    resp.addHeader("task-name", copiedTaskName);
                    responseMessageAndCode(resp, "The task " + taskName + " was copied successfully under " + copiedTaskName + " and started!", HttpServletResponse.SC_ACCEPTED);
                }
                else //Invalid incremental request - first time running the task cannot be incrementally
                    responseMessageAndCode(resp, "The task " + taskName + " cannot run incrementally for no previous runs!", HttpServletResponse.SC_BAD_REQUEST);
            }
            else //Run from scratch
            {
                String message;
                if(runBefore) //Need to make a copy of the task
                {
                    String copiedTaskName = tasksManager.copyTask(taskName, graphsManager, false);
                    tasksManager.startTask(copiedTaskName, userName, graphsManager);
                    resp.addHeader("task-name", copiedTaskName);
                    message = "The task " + taskName + " was copied successfully under " + copiedTaskName + " and started!";
                }
                else //First time running the task
                {
                    tasksManager.startTask(taskName, userName, ServletUtils.getGraphsManager(getServletContext()));
                    resp.addHeader("task-name", taskName);
                    message = "The task " + taskName + " started successfully!";
                }
                responseMessageAndCode(resp, message, HttpServletResponse.SC_ACCEPTED);
            }
        }
    }

    private synchronized void pauseTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String taskName = req.getParameter("pause-task");
        String adminName = req.getParameter("username");

        if(tasksManager.isTaskExists(taskName) && adminName != null && userManager.isAdmin(adminName))
        {
            if(tasksManager.isTaskPausable(taskName))
            {
                tasksManager.pauseTask(taskName, adminName);
                responseMessageAndCode(resp, taskName + " paused successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "Task can't be paused!", HttpServletResponse.SC_BAD_REQUEST);
        }
        else
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private synchronized void resumeTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String taskName = req.getParameter("resume-task");
        String adminName = req.getParameter("username");

        if(tasksManager.isTaskExists(taskName) && adminName != null && userManager.isAdmin(adminName))
        {
            if(tasksManager.isTaskResumable(taskName))
            {
                tasksManager.resumeTask(taskName, adminName);
                responseMessageAndCode(resp, taskName + " resumed successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "Task can't be resumed!", HttpServletResponse.SC_BAD_REQUEST);
        }
        else
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private synchronized void stopTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String taskName = req.getParameter("stop-task");
        String adminName = req.getParameter("username");

        if(tasksManager.isTaskExists(taskName) && adminName != null && userManager.isAdmin(adminName))
        {
            if(tasksManager.isTaskStoppable(taskName))
            {
                tasksManager.stopTask(taskName, adminName);
                responseMessageAndCode(resp, taskName + " stopped successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "Task can't be stopped!", HttpServletResponse.SC_BAD_REQUEST);
        }
        else
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private synchronized void updateTargetOnTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        ExecutedTargetUpdates updates = new Gson().fromJson(req.getReader(), ExecutedTargetUpdates.class);
        String taskName;
        String targetName;

        if(updates != null) //Valid request
        {
            tasksManager.updateTargetInfoOnTask(updates, ServletUtils.getGraphsManager(getServletContext()));
            taskName = updates.getTaskName();
            targetName = updates.getTargetName();

            if(updates.getRuntimeStatus().equalsIgnoreCase("Finished")) //The task on target is finished
            {
                tasksManager.addCreditsToWorker(updates.getUsername(), taskName);
                tasksManager.getTaskThread(taskName).taskOnTargetFinished(targetName);
                tasksManager.writeTargetSummaryToFile(taskName, targetName);
            }

            if(tasksManager.isTaskFinished(taskName)) //The task is over
                taskFinished(taskName, tasksManager);

            responseMessageAndCode(resp, "Update received in server about " + taskName + " - " + targetName, HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid request
            responseMessageAndCode(resp, "Invalid upload of updates!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private synchronized void taskFinished(String taskName, TasksManager tasksManager) {
        tasksManager.getGraphSummary(taskName).stopTheClock();
        tasksManager.removeTaskThread(taskName);
        tasksManager.removeTaskFromActiveList(taskName);
        tasksManager.removeAllWorkersRegistrationsFromTask(taskName);
        tasksManager.removeWorkersWithNoHistoryFromTask(taskName);

        String summary = createTaskSummary(taskName, tasksManager);
        tasksManager.writeTaskSummaryToFile(taskName);
    }

    private synchronized String createTaskSummary(String taskName, TasksManager tasksManager) {
        String summary = tasksManager.getTaskSummary(taskName);
        tasksManager.getAllTaskDetails(taskName).addToTaskLogHistory(summary);

        return summary;
    }

    //------------------------------------------------- General -------------------------------------------------//
    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}