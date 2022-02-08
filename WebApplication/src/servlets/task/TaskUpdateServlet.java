package servlets.task;

import com.google.gson.Gson;
import dtos.TaskCurrentInfoDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import managers.UserManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TaskUpdateServlet", urlPatterns = "/task-update")
public class TaskUpdateServlet extends HttpServlet {

    public Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        String taskName;

        if(req.getParameter("task-update") != null)
        {
            taskName = req.getParameter("task-update");

            if(tasksManager.isTaskExists(taskName)) //Returning task current information to admin
            {
                TaskCurrentInfoDTO updatedInfo = tasksManager.getTaskCurrentInfo(taskName);
                String infoAsString = this.gson.toJson(updatedInfo, TaskCurrentInfoDTO.class);
                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists
            {
                resp.addHeader("message", "The task " + taskName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("task-register") != null)
        {
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
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
            else //Valid registration to task
            {
                tasksManager.registerWorkerToTask(taskName, workerName);
                resp.addHeader("message", "Registered successfully to " + taskName + "!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        }
        else //Invalid request
        {
            resp.addHeader("message", "Invalid request!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
