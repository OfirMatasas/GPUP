package servlets.task;

import com.google.gson.Gson;
import dtos.DashboardTaskDetailsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;
import managers.TasksManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksServlet", urlPatterns = "/tasks")
public class TasksServlet extends HttpServlet {
    //---------------------------------------------------Members---------------------------------------//
    private Gson gson = new Gson();

    //----------------------------------------------------doGet----------------------------------------//

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getParameter("task-info") != null)
        {
            String taskInfoName = req.getParameter("task-info");
            String infoAsString;

            if(tasksManager.isTaskExists(taskInfoName))
            {
                DashboardTaskDetailsDTO taskInfo = tasksManager.getTaskDetailsDTO(taskInfoName);
                infoAsString = this.gson.toJson(taskInfo, DashboardTaskDetailsDTO.class);

                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists in the system
            {
                resp.getWriter().println("The task " + taskInfoName + " doesn't exist in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getParameter("task") != null) //Requesting for task-info
        {
            String taskName = req.getParameter("task");

            if(tasksManager.isTaskExists(taskName)) //The task exists in the system
            {
                String infoAsString = null;

                if(tasksManager.isSimulationTask(taskName)) //Requesting for simulation task
                {
                    SimulationTaskInformation simulationInfo = tasksManager.getSimulationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(simulationInfo, SimulationTaskInformation.class);

                    resp.addHeader("task-type", "simulation");
                }
                else  //Requesting for compilation task
                {
                    CompilationTaskInformation compilationInfo = tasksManager.getCompilationTaskInformation(taskName);
                    infoAsString = this.gson.toJson(compilationInfo, CompilationTaskInformation.class);

                    resp.addHeader("task-type", "compilation");
                }

                resp.getWriter().write(infoAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //Task not exists in the system
            {
                resp.getWriter().println("Task not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else //Invalid request
        {
            resp.getWriter().println("Invalid parameter!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //----------------------------------------------------doPost----------------------------------------//
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());

        if(req.getHeader("simulation") != null) //Uploaded simulation task
        {
            SimulationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), SimulationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addSimulationTask(newTaskInfo);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);

                tasksManager.addTaskDetailsDTO(newTaskInfo.getTaskName(), newTaskInfo.getTaskCreator(), "Simulation", newTaskInfo.getTargetsToExecute(),
                        ServletUtils.getGraphsManager(getServletContext()).getGraph(newTaskInfo.getGraphName()));
            }
            else //A task with the same name already exists in the system
            {
                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getHeader("compilation") != null) //Uploaded compilation task
        {
            CompilationTaskInformation newTaskInfo = this.gson.fromJson(req.getReader(), CompilationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addCompilationTask(newTaskInfo);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);

                tasksManager.addTaskDetailsDTO(newTaskInfo.getTaskName(), newTaskInfo.getTaskCreator(), "Compilation", newTaskInfo.getTargetsToExecute(),
                        ServletUtils.getGraphsManager(getServletContext()).getGraph(newTaskInfo.getGraphName()));            }
            else //A task with the same name already exists in the system
            {
                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else //invalid header for uploading new task to system
        {
            resp.addHeader("message", "Error in uploading task to server!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
