package utils;

import jakarta.servlet.ServletContext;
import servlets.graph.GraphsManager;

public class ServletUtils {

    private static final String GRAPHS_MANAGER_ATTRIBUTE_NAME = "graphsManager";

    /*
    Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
    the actual fetch of them is remained un-synchronized for performance POV
     */
    private static final Object graphsManagerLock = new Object();

    public static GraphsManager getGraphsManager(ServletContext servletContext) {

        synchronized (graphsManagerLock) {
            if (servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new GraphsManager());
            }
        }
        return (GraphsManager) servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME);
    }
}
