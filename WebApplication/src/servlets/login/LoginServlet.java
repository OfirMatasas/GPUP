package servlets.login;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final Set<String> loggedInUsers = new HashSet<>();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("in login servlet");
        String userName = req.getParameter("username");

        if(this.loggedInUsers.contains(userName.toLowerCase(Locale.ROOT)))
        {
            resp.getWriter().println("User name already exists.");
            resp.setStatus(300);
        }
        else
        {
            this.loggedInUsers.add(userName.toLowerCase(Locale.ROOT));
            resp.getWriter().println("Logged in successfully!");
            resp.addHeader("username", userName);
            resp.setStatus(200);
        }
    }
}
