package servlets.login;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "LoginServlet" , urlPatterns = "/user/login")
public class LoginServlet extends HttpServlet {

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName;
        boolean isAdmin;

        if(req.getParameter("adminUsername") != null)
        {
            isAdmin = true;
            userName = req.getParameter("adminUsername");
        }
        else
        {
            isAdmin = false;
            userName = req.getParameter("workerUsername");
        }

        userName = userName.trim();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        resp.setContentType("text/plain");

//        if(userManager.isUserExists(userName)){
//            resp.getWriter().println("The chosen user name is taken");
//            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//        }
        if(!userManager.isValidLogin(userName, isAdmin))
        {
            resp.getWriter().println("This user name is already taken by " + (isAdmin ? "a worker" : "an admin") + "!");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        else {
            req.getSession(true).setAttribute("username", userName);
            if(isAdmin)
                userManager.addAdmin(userName);
            else
                userManager.addWorker(userName);
            resp.getWriter().println("Logged in successfully");
            resp.addHeader("username", userName);
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}