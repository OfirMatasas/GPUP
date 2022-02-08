package servlets.task;

import com.google.gson.Gson;
import dtos.TaskCurrentInfoDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import managers.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Set;

@WebServlet(name = "TaskUpdateServlet", urlPatterns = "/task-update")
public class TaskUpdateServlet extends HttpServlet {

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        String taskName;

        if(req.getParameter("task-update") != null)
        {
            taskName = req.getParameter("task-update");

            if(tasksManager.isTaskExists(taskName)) //Returning task current information to admin
            {
                TaskCurrentInfoDTO updatedInfo = tasksManager.getTaskCurrentInfo(taskName);
                String infoAsString = gson.toJson(updatedInfo, TaskCurrentInfoDTO.class);
                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists
            {
                resp.addHeader("message", "The task " + taskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("register") != null) //Adding worker to task
        {
            String workerName = req.getParameter("username");
            taskName = req.getParameter("register");

            if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            {
                resp.addHeader("message", "Invalid task name!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
            {
                resp.addHeader("message", "Invalid username!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else if(tasksManager.isWorkerRegisteredToTask(workerName, taskName)) //Worker is already registered to task
            {
                resp.addHeader("message", "You're already registered to " + taskName +"!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else //Valid registration to task
            {
                tasksManager.registerWorkerToTask(taskName, workerName);
                resp.addHeader("message", "Registered successfully to " + taskName + "!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }
        else if(req.getParameter("unregister") != null) //Removing worker from task
        {
            String workerName = req.getParameter("username");
            taskName = req.getParameter("task-register");

            if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            {
                resp.addHeader("message", "Invalid task name!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
            {
                resp.addHeader("message", "Invalid user name!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else //Valid removing registration from task
            {
                tasksManager.removeWorkerRegistrationFromTask(taskName, workerName);
                resp.addHeader("message", "unregistered successfully to " + taskName + "!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }
        else if(req.getParameter("registered-tasks") != null) //Returning all worker's registered tasks
        {
            String workerName = req.getParameter("registered-tasks");

            if(workerName != null && userManager.isUserExists(workerName)) //Invalid worker name
            {
                Set<String> registeredTasks = tasksManager.getWorkerRegisteredTasks(workerName);
                String registeredTasksAsString = gson.toJson(registeredTasks, Set.class);
                resp.getWriter().write(registeredTasksAsString);

                resp.addHeader("message", "Successfully pulled worker's registered tasks!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Valid removing registration from task
            {
                resp.addHeader("message", "Invalid task name!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("credits") != null) //Returning worker's credits
        {
            String workerName = req.getParameter("credits");

            if(workerName != null && userManager.isUserExists(workerName)) //Valid credit request
            {
                resp.addHeader("credits", tasksManager.getWorkerCredits(workerName).toString());
                resp.addHeader("message", "Successfully pulled worker's credits!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Invalid request
            {
                resp.addHeader("message", "Invalid username!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else //Invalid request
        {
            resp.addHeader("message", "Invalid request!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String taskName, userName;
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("start-task") != null) //Requesting to start a task
        {
            taskName = req.getParameter("start-task");

            if(tasksManager.isTaskExists(taskName))
            {
                tasksManager.startTask(taskName);
            }
            else
            {
                resp.addHeader("message", "The task " + taskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else
        {
            resp.addHeader("message", "Invalid request!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
