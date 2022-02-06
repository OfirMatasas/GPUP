package servlets.task;

import com.google.gson.Gson;
import dtos.TaskCurrentInfoDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TaskUpdateServlet", urlPatterns = "/task-update")
public class TaskUpdateServlet extends HttpServlet {

    public Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String taskName = req.getParameter("task-update");

        if(taskName != null)
        {
            TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

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
        else //Invalid request
        {
            resp.addHeader("message", "Invalid request!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
