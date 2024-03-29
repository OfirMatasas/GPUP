package managers;

import target.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphsManager {

    private static final Map<String, File> graphsFileMap = new HashMap<>();
    private static final Map<String, Graph> actualGraphsMap = new HashMap<>();
    private static final Set<String> listOfGraphs = new HashSet<>();

    public boolean isGraphExists(String graphName) { return actualGraphsMap.containsKey(graphName.toLowerCase()); }

    public synchronized File getGraphFile(String graphName) {
        return graphsFileMap.get(graphName.toLowerCase());
    }

    public synchronized Graph getGraph(String graphName) {
        return actualGraphsMap.get(graphName.toLowerCase());
    }

    public synchronized void addGraph(String graphName, File file, Graph graph) {
        try {
            Path filePath = file.toPath();
            String newFileName = graph.getGraphName() + ".xml";
            Files.deleteIfExists(filePath.resolveSibling(newFileName));
            filePath = Files.move(filePath, filePath.resolveSibling(newFileName));

            graphsFileMap.put(graphName.toLowerCase(), filePath.toFile());
            actualGraphsMap.put(graphName.toLowerCase(), graph);
            listOfGraphs.add(graphName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Set<String> getGraphNames()
    {
        return listOfGraphs;
    }
}
