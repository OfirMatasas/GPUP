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
import managers.TasksManager;
import managers.UserManager;
import tableItems.WorkerChosenTargetInformationTableItem;
import tableItems.WorkerChosenTaskInformationTableItem;
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
        else //Invalid request
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnWorkerExecutedTargets(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String workerName = req.getParameter("executed-targets");

        if(userManager.isUserExists(workerName)) //Valid worker name
        {
            Map<String, WorkerTaskHistory> workerTasksHistory = tasksManager.getWorkerTaskHistory(workerName);
            Set<String> executedTargets = new HashSet<>();
            for(String currTask : workerTasksHistory.keySet())
                for(String currTarget : workerTasksHistory.get(currTask).getTargets())
                    executedTargets.add(currTask + " - " + currTarget);

            resp.getWriter().write(new Gson().toJson(executedTargets, Set.class));
            responseMessageAndCode(resp, "Successfully pulled worker's executed targets!", HttpServletResponse.SC_ACCEPTED);
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
            WorkerChosenTargetInformationTableItem tableItem = new WorkerChosenTargetInformationTableItem(targetName,
                    taskName, currInfo.getTaskType(), currInfo.getTaskStatus(), currInfo.getSinglePayment());
            String log = currInfo.getTargetLogHistory(targetName);
            WorkerChosenTargetDTO dto = new WorkerChosenTargetDTO(tableItem, log);

            resp.getWriter().write(new Gson().toJson(dto, WorkerChosenTargetDTO.class));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    private String getTaskNameFromWorkerChosenTargetRequest(String fullName) {
        int index = fullName.indexOf(" - ");

        if(index != -1)
            return fullName.substring(0, index);
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
            startTask(req, resp, tasksManager);
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

    private void startTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
        String taskName;
        String userName;
        taskName = req.getParameter("start-task");
        userName = req.getParameter("username");

        if(tasksManager.isTaskExists(taskName))
        {
            tasksManager.startTask(taskName, userName, ServletUtils.getGraphsManager(getServletContext()));
            responseMessageAndCode(resp, "The task " + taskName + " started successfully!", HttpServletResponse.SC_ACCEPTED);
        }
        else
            responseMessageAndCode(resp, "The task " + taskName + " doesn't exist in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void pauseTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
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

    private void resumeTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
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

    private void stopTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
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

    private void updateTargetOnTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
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
                tasksManager.addCreditsToWorker(updates.getUsername(), taskName, targetName);
                tasksManager.getTaskThread(taskName).taskOnTargetFinished(targetName);
            }

            if(tasksManager.isTaskFinished(taskName)) //The task is over
                taskFinished(taskName, tasksManager);

            responseMessageAndCode(resp, "Update received in server about " + taskName + " - " + targetName, HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid request
            responseMessageAndCode(resp, "Invalid upload of updates!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void taskFinished(String taskName, TasksManager tasksManager) {
        tasksManager.removeTaskThread(taskName);
        tasksManager.removeTaskFromActiveList(taskName);
        tasksManager.removeAllWorkersRegistrationsFromTask(taskName);
        tasksManager.removeWorkersWithNoHistoryFromTask(taskName);
    }

    //------------------------------------------------- General -------------------------------------------------//
    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}