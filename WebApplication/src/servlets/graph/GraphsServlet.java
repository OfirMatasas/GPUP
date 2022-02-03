package servlets.graph;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import resources.checker.ResourceChecker;
import target.Graph;
import utils.ServletUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

@WebServlet(name = "GraphsServlet", urlPatterns = "/graphs")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class GraphsServlet extends HttpServlet {

    public static Path WORKING_DIRECTORY_PATH = Paths.get("c:\\gpup-working-dir");
    private static final Object creatingDirectory = new Object();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("in graphs servlet - get");
        String graphName = req.getParameter("graphname");

        if(req.getParameterMap().containsKey(graphName))
        {

            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        else
        {
//            req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            synchronized (creatingDirectory)
            {
                if(!Files.exists(WORKING_DIRECTORY_PATH))
                    createWorkingDirectory();
            }

            Path filePath = Paths.get(WORKING_DIRECTORY_PATH + "\\CurrentGraph.xml");

            if(Files.exists(filePath))
                Files.delete(filePath);

            Files.createFile(filePath);
            System.out.println("in graphs servlet post - created graph file on server");

            Part filePart = req.getPart("fileToUpload");
            InputStream fileInputStream = filePart.getInputStream();
            Files.copy(fileInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            ResourceChecker rc = new ResourceChecker();
            Graph graph = rc.extractFromXMLToGraph(filePath);
            GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

            System.out.println("in graphs servlet post - graph created from xml file");

            if(graphsManager.isGraphExists(graph.getGraphName().toLowerCase(Locale.ROOT)))
            {
                resp.addHeader("message", "The graph " + graph.getGraphName() +" already exists in the system!");

                resp.getWriter().println("The graph " + graph.getGraphName() +" already exists in the system!");
                resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
            else
            {
                graph.setUploader(req.getHeader("username"));
                graphsManager.addGraph(graph.getGraphName(), filePath.toFile(), graph);

                resp.addHeader("message", "The graph " + graph.getGraphName() +" loaded successfully by " + req.getHeader("username") + "!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.addHeader("graphname", graph.getGraphName());
            }
        } catch (Exception e) {
            System.out.println("in graphs servlet post - failed in creating graph from xml");
            System.out.println(e.getMessage());
            resp.getWriter().println(e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createWorkingDirectory() throws IOException {
        Files.createDirectory(WORKING_DIRECTORY_PATH);
    }
}
