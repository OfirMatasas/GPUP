package utils;

import jakarta.servlet.ServletContext;
import target.GraphsManager;
import task.TasksManager;
import users.UserManager;

public class ServletUtils {

    private static final String GRAPHS_MANAGER_ATTRIBUTE_NAME = "graphsManager";
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String TASK_MANAGER_ATTRIBUTE_NAME = "taskManager";

    private static final Object graphsManagerLock = new Object();
    private static final Object userManagerLock = new Object();
    private static final Object taskManagerLock = new Object();

    public static GraphsManager getGraphsManager(ServletContext servletContext) {

        synchronized (graphsManagerLock) {
            if (servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new GraphsManager());
            }
        }
        return (GraphsManager)servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME);
    }

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager)servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static TasksManager getTasksManager(ServletContext servletContext) {
        synchronized (taskManagerLock) {
            if (servletContext.getAttribute(TASK_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(TASK_MANAGER_ATTRIBUTE_NAME, new TasksManager());
            }
        }
        return (TasksManager)servletContext.getAttribute(TASK_MANAGER_ATTRIBUTE_NAME);
    }
}