package servlets.login;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "LoginServlet" , urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    boolean isAdmin;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("adminUsername");
        if(userName!=null)
            this.isAdmin = true;
        else{
            userName = req.getParameter("workerUsername");
            this.isAdmin = false;
        }
        userName = userName.trim();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        resp.setContentType("text/plain");
        if(userManager.isUserExists(userName)){
            resp.getWriter().println("The chosen user name is taken");
            resp.setStatus(400);
        }
        else {
            req.getSession(true).setAttribute("username", userName);
            if(this.isAdmin)
                userManager.addAdmin(userName);
            else
                userManager.addWorker(userName);
            resp.getWriter().println("Logged in successfully");
            resp.addHeader("username", userName);
            resp.setStatus(200);
        }
    }
}
