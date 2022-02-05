package servlets.graph;

import com.google.gson.Gson;
import dtos.DashboardGraphDetailsDTO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import resources.checker.ResourceChecker;
import target.Graph;
import target.GraphsManager;
import utils.ServletUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "GraphsServlet", urlPatterns = "/graphs")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class GraphsServlet extends HttpServlet {
    //---------------------------------------------------Members---------------------------------------//

    public Gson gson = new Gson();
    public static Path WORKING_DIRECTORY_PATH = Paths.get("c:\\gpup-working-dir");
    private final Map<String, DashboardGraphDetailsDTO> graphDetailsDTOMap = new HashMap<>();
    //---------------------------------------------------Dummies---------------------------------------//
    private static final Object creatingDirectory = new Object();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

        if(req.getParameter("graph-details-DTO") != null)
        {
            String graphName = req.getParameter("graph-details-DTO");

            if(graphsManager.isGraphExists(graphName))
            {
                DashboardGraphDetailsDTO currDTO = this.graphDetailsDTOMap.get(graphName);
                String dtoAsString = this.gson.toJson(currDTO, DashboardGraphDetailsDTO.class);
                resp.getWriter().write(dtoAsString);

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else
            {
                resp.getWriter().println("Graph not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        else if (req.getParameter("graph") != null)
        {
            String graphName = req.getParameter("graph");

            if(graphsManager.isGraphExists(graphName))
            {
                File graphFile = graphsManager.getGraphFile(graphName);
                String fileAsString = this.gson.toJson(graphFile, File.class);
                resp.getWriter().write(fileAsString);

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
            else
            {
                resp.getWriter().println("Graph not exists!");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
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
                this.graphDetailsDTOMap.put(graph.getGraphName(), new DashboardGraphDetailsDTO(taskName, graph));

                resp.addHeader("message", "The graph " + graph.getGraphName() +" loaded successfully!");
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.addHeader("graphname", graph.getGraphName());
            }
        } catch (Exception e) {
            System.out.println("in graphs servlet post - failed in creating graph from xml");
            System.out.println(e.getMessage());
            resp.addHeader("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void createWorkingDirectory() throws IOException {
        Files.createDirectory(WORKING_DIRECTORY_PATH);
    }
}
