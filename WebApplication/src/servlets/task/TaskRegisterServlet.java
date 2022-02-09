package servlets.task;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import managers.UserManager;
import utils.ServletUtils;

@WebServlet(name = "TaskRegisterServlet", urlPatterns = "/task/register")
public class TaskRegisterServlet extends HttpServlet {

    //----------------------------------------------- Post -----------------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if(req.getParameter("register") != null) //Adding worker to task
            registerToTask(req, resp, tasksManager, userManager);
        else if(req.getParameter("unregister") != null) //Removing worker from task
            unregisterFromTask(req, resp, tasksManager, userManager);
        else //Invalid request
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void registerToTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) {
        String taskName = req.getParameter("register");
        String workerName = req.getParameter("username");

        if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else if(tasksManager.isWorkerRegisteredToTask(workerName, taskName)) //Worker is already registered to task
            responseMessageAndCode(resp, "You're already registered to " + taskName +"!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid registration to task
        {
            tasksManager.registerWorkerToTask(taskName, workerName);
            responseMessageAndCode(resp, "Registered successfully to " + taskName + "!", HttpServletResponse.SC_ACCEPTED);
        }
    }

    private void unregisterFromTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) {
        String taskName = req.getParameter("unregister");
        String workerName = req.getParameter("username");

        if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid removing registration from task
        {
            tasksManager.removeWorkerRegistrationFromTask(taskName, workerName);
            responseMessageAndCode(resp, "Unregistered successfully from " + taskName + "!", HttpServletResponse.SC_ACCEPTED);
        }
    }

    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}
