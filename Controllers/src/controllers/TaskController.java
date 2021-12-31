package controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    private Graph graph;
    private Map<String, TaskParameters> taskParametersMap;

    @FXML
    private BorderPane taskBorderPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private HBox toolBarHbox;

    @FXML
    private Button runButton;

    @FXML
    private Button PauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button deselectAllButton;

    @FXML
    private Pane leftPane;

    @FXML
    private ComboBox<?> taskSelection;

    @FXML
    private ComboBox<?> targetSelection;

    @FXML
    private ComboBox<?> affectedTargets;

    @FXML
    private ListView<?> currentSelectedTargetListView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tableViewTabPane;

    @FXML
    private TableView<?> taskTargetDetailsTableView;

    @FXML
    private TableColumn<?, ?> numberColumn;

    @FXML
    private TableColumn<?, ?> targetNameColumn;

    @FXML
    private TableColumn<?, ?> positionColumn;

    @FXML
    private TableColumn<?, ?> directDependsOnColumn;

    @FXML
    private TableColumn<?, ?> directRequiredForColumn;

    @FXML
    private TableColumn<?, ?> executionTimeStatus;

    @FXML
    private TableColumn<?, ?> finalStatus;

    @FXML
    private Tab graphViewTabPane;

    @FXML
    private Tab graphViewTabPane1;

    @FXML
    private Pane footerPane;

    @FXML
    private Label proccesingTimeLabel;

    @FXML
    private Label limitedPermanentLabel;

    @FXML
    private Label successRateLabel;

    @FXML
    private Label successRateWithWarnings;

    @FXML
    private Spinner<?> processingTimeSpinner;

    @FXML
    private RadioButton limitedRadioButton;

    @FXML
    private ToggleGroup limitedOrPermanent;

    @FXML
    private RadioButton permanentRadioButton;

    @FXML
    private Slider successRateSlider;

    @FXML
    private Slider successRatewithWarningsSlider;

    @FXML
    private Label successRateValueLabel;

    @FXML
    private Label successRateWithWarningsValueLabel;

    @FXML
    void affectedTargetsPressed(ActionEvent event) {

    }

    @FXML
    void deselectAllPressed(ActionEvent event) {

    }

    @FXML
    void graphViewTabPressed(Event event) {}

    @FXML
    void limitedOptionPressed(ActionEvent event) {

    }

    @FXML
    void logViewTabPressed(Event event) {

    }

    @FXML
    void pausePressed(ActionEvent event) {

    }

    @FXML
    void permanentOptionPressed(ActionEvent event) {

    }

    @FXML
    void runPressed(ActionEvent event) throws FileNotFoundException, OpeningFileCrash {
        Set<String> targetSet = new HashSet<>();

        getTaskParametersForAllTargets();
        for (Target target : graph.getGraphTargets().values())
            targetSet.add(target.getTargetName());

        TaskThread taskThread = new TaskThread(graph, TaskThread.TaskType.Simulation, taskParametersMap, new GraphSummary(graph, null),
                targetSet, 10);
        taskThread.start();
    }

    @FXML
    void selectAllPressed(ActionEvent event) {

    }

    @FXML
    void stopPressed(ActionEvent event) {

    }

    @FXML
    void tableViewTabPressed(Event event) {

    }

    @FXML
    void targetSelectionPressed(ActionEvent event) {

    }

    @FXML
    void taskSelectionPressed(ActionEvent event) {

    }

    public void setGraph(Graph graph) {
        this.graph = graph;
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
