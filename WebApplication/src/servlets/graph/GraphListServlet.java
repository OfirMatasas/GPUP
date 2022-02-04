package servlets.graph;


import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import target.GraphsManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Set;

@WebServlet(name = "GraphListServlet", urlPatterns = "/graph-list")
public class GraphListServlet extends HttpServlet {

    private static final Object dummy = new Object();

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if(req.getParameter("graph-list") != null)
        {
            GraphsManager graphsManager;
            String listAsString;

            synchronized (dummy)
            {
                graphsManager = ServletUtils.getGraphsManager(getServletContext());
            }
            Set<String> graphNamesSet = graphsManager.getGraphNames();

            if(!graphNamesSet.isEmpty())
            {
                listAsString = this.gson.toJson(graphsManager.getGraphNames());

                resp.getWriter().println(listAsString);
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        else
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
