package servlets.task;


import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TaskListServlet", urlPatterns = "/task/list")
public class TaskListServlet extends HttpServlet {

    private static final Object dummy = new Object();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        TasksManager tasksManager;

        tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("all-tasks-list") != null) //Returning all tasks list (for admins)
        {
            resp.getWriter().println(gson.toJson(tasksManager.getAllTasksList()));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(req.getParameter("my-tasks-list") != null) //Returning admins tasks list
        {
            resp.getWriter().println(gson.toJson(tasksManager.getUserTaskList(req.getParameter("username"))));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(req.getParameter("active-tasks-list") != null) //Returning all active tasks list (for workers)
        {
            resp.getWriter().println(gson.toJson(tasksManager.getActiveTasksList()));
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else //Invalid request
        {
            resp.addHeader("message", "Invalid request!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
