package servlets.task;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;
import task.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksServlet", urlPatterns = "/tasks")
public class TasksServlet extends HttpServlet {
    //---------------------------------------------------Members---------------------------------------//
    public Gson gson = new Gson();

    //----------------------------------------------------doGet----------------------------------------//

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("task-info") != null)
        {
            String taskName = req.getParameter("task-info");

            if(tasksManager.isTaskExists(taskName))
            {
                String infoAsString = null;

                if(req.getParameter("simulation") != null)
                {
                    SimulationTaskInformation simulationInfo = tasksManager.getSimulationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(simulationInfo, SimulationTaskInformation.class);
                }
                else if(req.getParameter("compilation") != null)
                {
                    CompilationTaskInformation compilationInfo = tasksManager.getCompilationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(compilationInfo, CompilationTaskInformation.class);
                }

                if(infoAsString != null)
                {
                    resp.getWriter().write(infoAsString);
                    resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
                else
                {
                    resp.getWriter().println("Invalid task type!");
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            else
            {
                resp.getWriter().println("Task not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else
        {
            resp.getWriter().println("Invalid parameter!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //----------------------------------------------------doPost----------------------------------------//
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        System.out.println("in tasks servlet - post");

        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getHeader("simulation") != null)
        {
            SimulationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), SimulationTaskInformation.class);
            tasksManager.addSimulationTask(newTaskInfo);

            resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else if(req.getHeader("compilation") != null)
        {
            CompilationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), CompilationTaskInformation.class);
            tasksManager.addCompilationTask(newTaskInfo);

            resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else
        {
            resp.addHeader("message", "Error in uploading task to server!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
