package task;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskThread extends Thread {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public static enum TaskType { Simulation, Compilation }

    //-------------------------------------------------Members------------------------------------------------------//
    //Getting from UI
    private Graph graph;
    private TaskType taskType;
    private Map<String, TaskParameters> taskParametersMap;
    private GraphSummary graphSummary;
    private Set<String> targetsSet;

    //Made locally
    private ExecutorService executor;
    private TaskOutput taskOutput;

    //-----------------------------------------------Constructor----------------------------------------------------//
    public TaskThread(Graph graph, TaskType taskType, Map<String, TaskParameters> taskParametersMap,
                      GraphSummary graphSummary, Set<String> targetsSet, int threadsNum) throws FileNotFoundException, OpeningFileCrash {
        this.graph = graph;
        this.taskType = taskType;
        this.taskParametersMap = taskParametersMap;
        this.graphSummary = graphSummary;
        this.targetsSet = targetsSet;
        this.executor = Executors.newFixedThreadPool(Math.min(threadsNum, 10));
        this.taskOutput = new TaskOutput(taskType.toString(), graphSummary);
    }

    //-------------------------------------------------Methods------------------------------------------------------//
    @Override
    public void run()
    {
        if(taskType.equals(TaskType.Simulation))
        {
            for(String currTarget : targetsSet)
            {
                UpdateWorkingTime();

                //Starting task on graph
                taskOutput.printStartOfTaskOnGraph(graph.getGraphName());
                graphSummary.startTheClock();

                try {
                    executor.execute(new SimulationThread(taskParametersMap.get(currTarget), graph, graphSummary, currTarget, taskOutput));
                } catch (FileNotFoundException e) {
                    Platform.runLater(() -> ErrorPopup(e, "Error with " + currTarget + " file."));
                }
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {   }
    }

    private void ErrorPopup(Exception ex, String title)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }

    private void UpdateWorkingTime() {
        String currentTargetName;
        long timeLong;
        Duration timeDuration;
        TaskParameters currentTaskParameters;

        for(TargetSummary currentTargetSummary : graphSummary.getTargetsSummaryMap().values())
        {
            currentTargetName = graph.getTarget(currentTargetSummary.getTargetName()).getTargetName();
            currentTaskParameters = taskParametersMap.get(currentTargetName);

            if(currentTaskParameters.isRandom())
            {
                timeDuration = taskParametersMap.get(currentTaskParameters).getProcessingTime();
                timeLong = (long)(Math.random() * (timeDuration.toMillis())) + 1;
                timeDuration = Duration.of(timeLong, ChronoUnit.MILLIS);
                currentTargetSummary.setPredictedTime(timeDuration);
            }
        }
    }
}
