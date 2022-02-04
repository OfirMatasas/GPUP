package servlets.login;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import users.UsersLists;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "UsersListServlet", urlPatterns = "/userslists")
public class UsersListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("entered to users lists servlet doGet");
        resp.setContentType("application/json");
        Gson gson = new Gson();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        UsersLists usersLists = userManager.getUsersLists();
        String usersListsJson = gson.toJson(usersLists, UsersLists.class);
        resp.getWriter().write(usersListsJson);
    }

}
