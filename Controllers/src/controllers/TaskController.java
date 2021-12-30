package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import target.Graph;
import target.Target;
import task.TaskParameters;
import task.TaskThread;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TaskController {
    Graph graph;
    Map<String, TaskParameters> taskParametersMap;

    @FXML
    private GridPane TaskGridPane;

    @FXML
    private GridPane TaskToolbarGridPane;

    @FXML
    private ToolBar TaskLeftToolbar;

    @FXML
    private Button RunButton;

    @FXML
    private Button PauseButton;

    @FXML
    private Button StopButton;

    @FXML
    private ToolBar RightToolbar;

    @FXML
    private Button SelectAllButton;

    @FXML
    private Button DeselectAllButton;

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void startTask(ActionEvent actionEvent) throws FileNotFoundException, OpeningFileCrash {
        Set<String> targetSet = new HashSet<String>();

        getTaskParametersForAllTargets();
        for(Target target : graph.getGraphTargets().values())
            targetSet.add(target.getTargetName());

        TaskThread taskThread = new TaskThread(graph, TaskThread.TaskType.Simulation, taskParametersMap, new GraphSummary(graph, null),
                targetSet, 10);
        taskThread.start();
    }

    private void getTaskParametersForAllTargets()
    {
        taskParametersMap = new HashMap<>();
        TaskParameters taskParameters = getTaskParametersFromUser();

        for(Target target : graph.getGraphTargets().values())
            taskParametersMap.put(target.getTargetName(), taskParameters);
    }

    private TaskParameters getTaskParametersFromUser()
    {
        Scanner scanner = new Scanner(System.in);
        Duration processingTime = null;
        long timeInMS = -1;
        Boolean isRandom = true;
        Double successRate = -1.0, successWithWarnings = -1.0;
        TaskParameters taskParameters = new TaskParameters();
//
//        System.out.print("Enter the processing time (in m/s) for each task: ");
//        timeInMS = scanner.nextLong();
//        processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);
//
//        System.out.print("Choose if the processing time is limited by the value you just entered, or permanent (0 - limited, 1 - permanent): ");
//        int temp = scanner.nextInt();
//        isRandom = temp == 0;
//
//        System.out.print("Enter the success rate of the task (value between 0 and 1): ");
//        successRate = scanner.nextDouble();
//
//        System.out.print("If the task ended successfully, what is the chance that it ended with warnings? (value between 0 and 1): ");
//        successWithWarnings = scanner.nextDouble();

        timeInMS = 5000;
        processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);
        isRandom = true;
        successRate = 0.7;
        successWithWarnings = 0.3;

        taskParameters.setProcessingTime(processingTime);
        taskParameters.setRandom(isRandom);
        taskParameters.setSuccessRate(successRate);
        taskParameters.setSuccessWithWarnings(successWithWarnings);

        return taskParameters;
    }
}
