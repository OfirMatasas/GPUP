package utils;

import dtos.DashboardGraphDetailsDTO;
import jakarta.servlet.ServletContext;
import target.Graph;
import target.GraphsManager;

import java.util.HashMap;

public class ServletUtils {

    private static final String GRAPHS_MANAGER_ATTRIBUTE_NAME = "graphsManager";

    private static final Object graphsManagerLock = new Object();

    public static GraphsManager getGraphsManager(ServletContext servletContext) {

        synchronized (graphsManagerLock) {
            if (servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new GraphsManager());
            }
        }
        return (GraphsManager) servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME);
    }

    public static void addGraphDetailsDTO(ServletContext servletContext, Graph graph)
    {
        if (servletContext.getAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME) == null) {
            servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new HashMap<>());
        }

        servletContext.setAttribute(GRAPHS_MANAGER_ATTRIBUTE_NAME, new DashboardGraphDetailsDTO(graph));
    }
}