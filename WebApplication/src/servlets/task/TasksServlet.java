package servlets.task;

import com.google.gson.Gson;
import information.AllTaskDetails;
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

        if(req.getParameter("task-info") != null) //Requesting for task-info
            doGetTaskInfo(req, resp, tasksManager);
        else if(req.getParameter("task") != null) //Requesting for task
            doGetTask(req, resp, tasksManager);
        else if(req.getParameter("execute") != null) //Requesting for target to execute (worker)
            doGetExecute(req, resp, tasksManager, userManager);
        else //Invalid request
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void doGetTaskInfo(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        String taskName = req.getParameter("task-info");
        String infoAsString;

        if(tasksManager.isTaskExists(taskName))
        {
            AllTaskDetails taskInfo = tasksManager.getAllTaskDetails(taskName);
            infoAsString = new Gson().toJson(taskInfo, AllTaskDetails.class);

            resp.getWriter().write(infoAsString);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else //Task not exists in the system
        {
            resp.getWriter().println("The task " + taskName + " doesn't exist in the system!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void doGetTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) throws IOException {
        String taskName = req.getParameter("task");

        if(tasksManager.isTaskExists(taskName)) //The task exists in the system
        {
            String infoAsString;
            String taskOriginalName = tasksManager.getOriginalTaskName(taskName);

            if(tasksManager.isSimulationTask(taskOriginalName)) //Requesting for simulation task
            {
                SimulationTaskInformation simulationInfo = tasksManager.getSimulationTaskInformation(taskOriginalName);
                infoAsString = new Gson().toJson(simulationInfo, SimulationTaskInformation.class);

                resp.addHeader("task-type", "simulation");
            }
            else  //Requesting for compilation task
            {
                CompilationTaskInformation compilationInfo = tasksManager.getCompilationTaskInformation(taskOriginalName);
                infoAsString = new Gson().toJson(compilationInfo, CompilationTaskInformation.class);

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

    private void doGetExecute(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, UserManager userManager) throws IOException {
        String taskName = req.getParameter("execute");
        String workerName = req.getParameter("username");

        if(taskName == null || !tasksManager.isTaskExists(taskName)) //Invalid task name
            responseMessageAndCode(resp, "Invalid task name!", HttpServletResponse.SC_BAD_REQUEST);
        else if(workerName == null || !userManager.isUserExists(workerName)) //Invalid username
            responseMessageAndCode(resp, "Invalid username!", HttpServletResponse.SC_BAD_REQUEST);
        else if(!tasksManager.isWorkerRegisteredToTask(workerName, taskName)) //Not registered
            responseMessageAndCode(resp, "Not assigned to the task!", HttpServletResponse.SC_BAD_REQUEST);
        else //Valid request
            returnTargetToExecuteToWorker(resp, tasksManager, taskName, workerName);
    }

    private void returnTargetToExecuteToWorker(HttpServletResponse resp, TasksManager tasksManager, String taskName, String workerName) throws IOException {
        String parametersAsString;
        String originalTaskName = tasksManager.getOriginalTaskName(taskName);

        if(!tasksManager.isTaskRunning(taskName)) //Task not running
            responseMessageAndCode(resp, "Task not running at the moment.", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        else if(tasksManager.isSimulationTask(originalTaskName))  //Simulation task
        {
            SimulationTaskInformation taskInfo = tasksManager.getSimulationTaskInformation(originalTaskName);
            String targetName = tasksManager.getTaskThread(taskName).getWaitingTargetToExecute();

            if(targetName != null) //Returning target to execute
            {
                WorkerSimulationParameters parameters = new WorkerSimulationParameters(taskName, targetName, workerName,
                        taskInfo.getSimulationParameters());
                tasksManager.addTargetToWorkerTaskHistory(workerName, taskName, targetName);
                parametersAsString = new Gson().toJson(parameters, WorkerSimulationParameters.class);
                resp.getWriter().write(parametersAsString);
                resp.addHeader("task-type", "Simulation");
                responseMessageAndCode(resp, "Pulled target successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else //No targets to execute at the moment
                responseMessageAndCode(resp, "No targets to execute at the moment", HttpServletResponse.SC_SERVICE_UNAVAILABLE);

        }
        else if(tasksManager.isCompilationTask(taskName))//Compilation task
        {
            CompilationTaskInformation taskInfo = tasksManager.getCompilationTaskInformation(originalTaskName);
            String targetName = taskInfo.getTargetToExecute();

            if(targetName != null) //Returning target to execute
            {
                WorkerCompilationParameters parameters = new WorkerCompilationParameters(taskName, targetName, workerName,
                        taskInfo.getCompilationParameters());
                parametersAsString = new Gson().toJson(parameters, WorkerCompilationParameters.class);
                resp.getWriter().write(parametersAsString);
                resp.addHeader("task-type", "Compilation");
                responseMessageAndCode(resp, "Pulled target successfully!", HttpServletResponse.SC_ACCEPTED);
            }
            else //No targets to execute at the moment
                responseMessageAndCode(resp, "No targets to execute at the moment", HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        else //Invalid task type
            responseMessageAndCode(resp, "Unknown task type", HttpServletResponse.SC_BAD_REQUEST);
    }

    //------------------------------------------------- Post -------------------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
        GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

        if(req.getHeader("simulation") != null) //Uploaded simulation task
            doPostSimulation(req, resp, tasksManager, graphsManager);
        else if(req.getHeader("compilation") != null) //Uploaded compilation task
            doPostCompilation(req, resp, tasksManager, graphsManager);
//        else if(req.getParameter("rerun-task") != null)
//            doPostRerunTask(req, resp, tasksManager);
        else //invalid header for uploading new task to system
            responseMessageAndCode(resp, "Error in uploading task to server!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void doPostSimulation(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, GraphsManager graphsManager) throws IOException {
        SimulationTaskInformation newTaskInfo = new Gson().fromJson(req.getReader(), SimulationTaskInformation.class);
        if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
        {
            tasksManager.addSimulationTask(newTaskInfo, graphsManager);
            responseMessageAndCode(resp, "The task " + newTaskInfo.getTaskName() + " uploaded successfully!", HttpServletResponse.SC_ACCEPTED);
        }
        else //A task with the same name already exists in the system
            responseMessageAndCode(resp, "The task " + newTaskInfo.getTaskName() + " already exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void doPostCompilation(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager, GraphsManager graphsManager) throws IOException {
        CompilationTaskInformation newTaskInfo = new Gson().fromJson(req.getReader(), CompilationTaskInformation.class);
        if(!tasksManager.isTaskExists(newTaskInfo.getTaskName())) //No task with the same name was found
        {
            tasksManager.addCompilationTask(newTaskInfo, graphsManager);
            responseMessageAndCode(resp, "The task " + newTaskInfo.getTaskName() + " uploaded successfully!", HttpServletResponse.SC_ACCEPTED);
       }
        else //A task with the same name already exists in the system
            responseMessageAndCode(resp, "The task " + newTaskInfo.getTaskName() + " already exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

//    private void doPostRerunTask(HttpServletRequest req, HttpServletResponse resp, TasksManager tasksManager) {
//        String taskName = req.getParameter("rerun-task");
//        String newTaskName;
//
//        if(tasksManager.isTaskExists(taskName))
//        {
//            newTaskName = tasksManager.copyAndRunTask(taskName, ServletUtils.getGraphsManager(getServletContext()));
//            resp.addHeader("taskName", newTaskName);
//            responseMessageAndCode(resp, "The task " + taskName + " was copied to " + newTaskName +
//                    " and successfully started!", HttpServletResponse.SC_ACCEPTED);
//        }
//        else
//            responseMessageAndCode(resp, "The task " + taskName + " not exists!", HttpServletResponse.SC_BAD_REQUEST);
//    }

    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }
}