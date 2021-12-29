package task;

import summaries.GraphSummary;
import target.Graph;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskThread extends Thread {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public static enum TaskType { Simulation, Compilation }

    //-------------------------------------------------Members------------------------------------------------------//
    Graph graph;
    TaskType taskType;
    Map<String, TaskParameters> taskParametersMap;
    GraphSummary graphSummary;
    Set<String> selectedTargets;
    ExecutorService executor;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, TaskParameters> taskParametersMap,
                      GraphSummary graphSummary, Set<String> selectedTargets, int parallelThreads) {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.graphSummary = graphSummary;
        this.selectedTargets = selectedTargets;
        this.executor = Executors.newFixedThreadPool(Math.min(parallelThreads, 10));
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        if(taskType.equals(TaskType.Simulation))
        {
            executor.execute(new SimulationThread(taskParametersMap.get("A"), graph, graphSummary));
        }



        executor.shutdown();
        while (!executor.isTerminated()) {   }
    }
}
