package servlets.task;

import com.google.gson.Gson;
import dtos.DashboardTaskDetailsDTO;
import dtos.WorkerChosenTaskDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import managers.UserManager;
import tableItems.WorkerChosenTaskInformationTableItem;
import utils.ServletUtils;

import java.io.IOException;
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
        else if(req.getParameter("credits") != null) //Returning worker's credits
            returnWorkerCurrentCredits(req, resp, tasksManager, userManager);
        else if(req.getParameter("chosen-task") != null)
            returnWorkerChosenTask(req, resp, tasksManager, userManager);
        else //Invalid request
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
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
            DashboardTaskDetailsDTO currInfo = tasksManager.getTaskDetailsDTO(taskName);

            WorkerChosenTaskInformationTableItem tableItem = new WorkerChosenTaskInformationTableItem(taskName,
                    currInfo.getTaskStatus(), currInfo.getRegisteredWorkersNumber(), currInfo.getFinishedTargets(), tasksManager.getWorkerCredits(workerName));

            WorkerChosenTaskDTO returnedDTO = new WorkerChosenTaskDTO(tableItem, currInfo.getTargetStatusSet().size(), currInfo.getFinishedTargets());

            resp.getWriter().write(new Gson().toJson(returnedDTO, WorkerChosenTaskDTO.class));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
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
        Gson gson = new Gson();

        if(workerName != null && userManager.isUserExists(workerName)) //Worker exists in the system
        {
            Set<String> registeredTasks = tasksManager.getWorkerRegisteredTasks(workerName);
            String registeredTasksAsString = gson.toJson(registeredTasks, Set.class);
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
            DashboardTaskDetailsDTO updatedInfo = tasksManager.getTaskDetailsDTO(taskName);
            String infoAsString = gson.toJson(updatedInfo, DashboardTaskDetailsDTO.class);
            resp.getWriter().write(infoAsString);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else //Task not exists
            responseMessageAndCode(resp, "The task " + taskName + " doesn't exist in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    //------------------------------------------------- Post -------------------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String taskName, userName;
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("start-task") != null) //Requesting to start a task
            startTask(req, resp, tasksManager);
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

    //------------------------------------------------- General -------------------------------------------------//
    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}