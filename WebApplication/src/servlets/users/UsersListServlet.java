package servlets.users;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import users.UsersLists;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "UsersListServlet", urlPatterns = "/user/list")
public class UsersListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UsersLists usersLists = userManager.getUsersLists();
        String usersListsJson = new Gson().toJson(usersLists, UsersLists.class);

        resp.getWriter().write(usersListsJson);
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
    }
}