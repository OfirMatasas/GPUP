package servlets.login;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.TasksManager;
import managers.UserManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = "/user/logout")
public class LogoutServlet extends HttpServlet {

    @Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        if (username != null && userManager.isUserExists(username))
        {
            if(!userManager.isAdmin(username)) //Worker logging out - unregister from all tasks
            {
                TasksManager tasksManager = ServletUtils.getTasksManager(getServletContext());
//                tasksManager.workerAbortedTasks(username);
                tasksManager.removeWorkerRegistrationFromAllTasks(username);
            }

            System.out.println("Clearing session for " + username);
            userManager.removeUser(username);

            response.getWriter().println(username + " is logged out!");
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
        else
        {
            response.getWriter().println("Invalid username!");
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}