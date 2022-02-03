package servlets.graph;

import target.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GraphsManager {

    private static final Map<String, File> graphsFileMap = new HashMap<>();
    private static final Map<String, Graph> actualGraphsMap = new HashMap<>();

    public boolean isGraphExists(String graphName) { return actualGraphsMap.containsKey(graphName.toLowerCase(Locale.ROOT)); }

    public synchronized File getGraphFile(String graphName) {
        return graphsFileMap.get(graphName);
    }

    public synchronized Graph getGraph(String graphName) {
        return actualGraphsMap.get(graphName);
    }

    public synchronized void addGraph(String graphName, File file, Graph graph) {
        graphsFileMap.put(graphName.toLowerCase(Locale.ROOT), file);
        actualGraphsMap.put(graphName.toLowerCase(Locale.ROOT), graph);

        Path filePath = file.toPath();
        String newFileName = graph.getGraphName() + ".xml";

        try {
            Files.deleteIfExists(filePath.resolveSibling(newFileName));
            Files.move(filePath, filePath.resolveSibling(newFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
