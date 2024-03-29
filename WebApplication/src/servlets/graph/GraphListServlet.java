package servlets.graph;


import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.GraphsManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Set;

@WebServlet(name = "GraphListServlet", urlPatterns = "/graph/list")
public class GraphListServlet extends HttpServlet {

    private static final Object dummy = new Object();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();

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
                listAsString = gson.toJson(graphsManager.getGraphNames());

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
}
