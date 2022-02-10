package servlets.task;

import com.google.gson.Gson;
import dtos.DashboardTaskDetailsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.GraphsManager;
import managers.TasksManager;
import managers.UserManager;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;
import task.WorkerCompilationParameters;
import task.WorkerSimulationParameters;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "TasksServlet", urlPatterns = "/task")
public class TasksServlet extends HttpServlet {
    //------------------------------------------------Members------------------------------------------------//

    //------------------------------------------------- Get -------------------------------------------------//
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        Gson gson = new Gson();

        if(req.getParameter("task-info") != null) //Requesting for task-info
        {
            String taskInfoName = req.getParameter("task-info");
            String infoAsString;

            if(tasksManager.isTaskExists(taskInfoName))
            {
                DashboardTaskDetailsDTO taskInfo = tasksManager.getTaskDetailsDTO(taskInfoName);
                infoAsString = gson.toJson(taskInfo, DashboardTaskDetailsDTO.class);

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
                String infoAsString;

                if(tasksManager.isSimulationTask(taskName)) //Requesting for simulation task
                {
                    SimulationTaskInformation simulationInfo = tasksManager.getSimulationTaskInformation(taskName);
                    infoAsString = gson.toJson(simulationInfo, SimulationTaskInformation.class);

                    resp.addHeader("task-type", "simulation");
                }
                else  //Requesting for compilation task
                {
                    CompilationTaskInformation compilationInfo = tasksManager.getCompilationTaskInformation(taskName);
                    infoAsString = gson.toJson(compilationInfo, CompilationTaskInformation.class);

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
        else if(req.getParameter("execute") != null) //Requesting for target to execute (worker)
        {
            String taskName = req.getParameter("execute");
            String workerName = req.getParameter("username");

            if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
                responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
            else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
                responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
            else if(!tasksManager.isWorkerRegisteredToTask(workerName, taskName)) //Not registered
                responseMessageAndCode(resp, "Not assigned to the task!", HttpServletResponse.SC_BAD_REQUEST);
            else //Valid request
                returnTargetToExecuteToWorker(resp, tasksManager, taskName, workerName, gson);
        }
        else //Invalid request
        {
            resp.getWriter().println("Invalid parameter!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void returnTargetToExecuteToWorker(HttpServletResponse resp, TasksManager tasksManager, String taskName, String workerName, Gson gson) throws IOException {
        String parametersAsString;

        if(tasksManager.isSimulationTask(taskName))
        {
            SimulationTaskInformation taskInfo = tasksManager.getSimulationTaskInformation(taskName);
            String targetName = tasksManager.getTaskThread(taskName).getWaitingTargetToExecute();

            if(targetName != null)
            {
                WorkerSimulationParameters parameters = new WorkerSimulationParameters(taskName, targetName, workerName,
                        taskInfo.getSimulationParameters());
                parametersAsString = gson.toJson(parameters, WorkerSimulationParameters.class);
                resp.getWriter().write(parametersAsString);
                resp.addHeader("task-type", "Simulation");
                responseMessageAndCode(resp, "Pulled target successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "No targets to execute at the moment", HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        }
        else if(tasksManager.isCompilationTask(taskName))
        {
            CompilationTaskInformation taskInfo = tasksManager.getCompilationTaskInformation(taskName);
            String targetName = taskInfo.getTargetToExecute();

            if(targetName != null)
            {
                WorkerCompilationParameters parameters = new WorkerCompilationParameters(taskName, targetName, workerName,
                        taskInfo.getCompilationParameters());
                parametersAsString = gson.toJson(parameters, WorkerCompilationParameters.class);
                resp.getWriter().write(parametersAsString);
                resp.addHeader("task-type", "Compilation");
                responseMessageAndCode(resp, "Pulled target successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else
                responseMessageAndCode(resp, "No targets to execute at the moment", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        else
            responseMessageAndCode(resp, "Unknown task type", HttpServletResponse.SC_BAD_REQUEST);
    }

    //------------------------------------------------- Post -------------------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());
        Gson gson = new Gson();

        if(req.getHeader("simulation") != null) //Uploaded simulation task
        {
            SimulationTaskInformation newTaskInfo = gson.fromJson(req.getReader(), SimulationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addSimulationTask(newTaskInfo, graphsManager);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else //A task with the same name already exists in the system
            {
                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if(req.getHeader("compilation") != null) //Uploaded compilation task
        {
            CompilationTaskInformation newTaskInfo = gson.fromJson(req.getReader(), CompilationTaskInformation.class);
            if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
            {
                tasksManager.addCompilationTask(newTaskInfo, graphsManager);

                resp.addHeader("message", "The task " + newTaskInfo.getTaskName() + " uploaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);

           }
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

    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}
