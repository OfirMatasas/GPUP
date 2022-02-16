package servlets.graph;

import com.google.gson.Gson;
import dtos.DashboardGraphDetailsDTO;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import managers.GraphsManager;
import resources.checker.ResourceChecker;
import target.Graph;
import utils.ServletUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "GraphsServlet", urlPatterns = "/graph")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class GraphsServlet extends HttpServlet {
    //-------------------------------------------- Members ---------------------------------------//
    public Gson gson = new Gson();
    public static Path WORKING_DIRECTORY_PATH = Paths.get("c:\\gpup-working-dir");
    private final Map<String, DashboardGraphDetailsDTO> graphDetailsDTOMap = new HashMap<>();

    //-------------------------------------------- Dummies ---------------------------------------//
    private static final Object creatingDirectory = new Object();

    //---------------------------------------------- Get -----------------------------------------//
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

        if(req.getParameter("graph-details-DTO") != null) //Requesting for graph details
            returnGraphDetails(req, resp, graphsManager);
        else if (req.getParameter("graph") != null) //Requesting for graph
            returnGraphFile(req, resp, graphsManager);
        else
            responseMessageAndCode(resp, "Invalid request!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnGraphDetails(HttpServletRequest req, HttpServletResponse resp, GraphsManager graphsManager) throws IOException {
        String graphName = req.getParameter("graph-details-DTO");

        if(graphsManager.isGraphExists(graphName)) //Graph exists = valid request
        {
            DashboardGraphDetailsDTO currDTO = this.graphDetailsDTOMap.get(graphName);
            String dtoAsString = this.gson.toJson(currDTO, DashboardGraphDetailsDTO.class);
            resp.getWriter().write(dtoAsString);

            responseMessageAndCode(resp, graphName + " information pulled successfully from the server!", HttpServletResponse.SC_ACCEPTED);
        }
        else //Graph not exists = invalid request
            responseMessageAndCode(resp, "The graph" + graphName + " not exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    private void returnGraphFile(HttpServletRequest req, HttpServletResponse resp, GraphsManager graphsManager) throws IOException {
        String graphName = req.getParameter("graph");

        if(graphsManager.isGraphExists(graphName))
        {
            File graphFile = graphsManager.getGraphFile(graphName);
            String fileAsString = this.gson.toJson(graphFile, File.class);
            resp.getWriter().write(fileAsString);

            responseMessageAndCode(resp, graphName + " file pulled successfully from the server!", HttpServletResponse.SC_ACCEPTED);
        }
        else
            responseMessageAndCode(resp, "The graph" + graphName + " not exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
    }

    //---------------------------------------------- Post -----------------------------------------//
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
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

            Part filePart = req.getPart("fileToUpload");
            InputStream fileInputStream = filePart.getInputStream();
            Files.copy(fileInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            ResourceChecker rc = new ResourceChecker();
            Graph graph = rc.extractFromXMLToGraph(filePath);
            GraphsManager graphsManager = ServletUtils.getGraphsManager(getServletContext());

            if(graphsManager.isGraphExists(graph.getGraphName()))
                responseMessageAndCode(resp, "The graph " + graph.getGraphName() +" already exists in the system!", HttpServletResponse.SC_BAD_REQUEST);
            else
            {
                graph.setUploader(req.getHeader("username"));
                graphsManager.addGraph(graph.getGraphName(), filePath.toFile(), graph);
                this.graphDetailsDTOMap.put(graph.getGraphName(), new DashboardGraphDetailsDTO(graph));

                resp.addHeader("graphname", graph.getGraphName());
                responseMessageAndCode(resp, "The graph " + graph.getGraphName() +" loaded successfully!", HttpServletResponse.SC_ACCEPTED);
            }
        } catch (Exception e) {responseMessageAndCode(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);}
    }

    //------------------------------------------------- General -------------------------------------------------//
    private void responseMessageAndCode(HttpServletResponse resp, String message, int code) {
        resp.addHeader("message", message);
        resp.setStatus(code);
    }

    private void createWorkingDirectory() throws IOException {
        Files.createDirectory(WORKING_DIRECTORY_PATH);
    }
}
