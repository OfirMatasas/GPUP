package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import target.Graph;
import target.Target;
import task.TaskParameters;
import task.TaskThread;

import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskController implements Initializable {
    private Graph graph;
    private Map<String, TaskParameters> taskParametersMap = new HashMap<>();
    private int parallelThreads;
    private ExecutorService executor;

    @FXML
    private BorderPane taskBorderPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private HBox toolBarHBox;

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
    private ComboBox<String> taskSelection;

    @FXML
    private ComboBox<String> targetSelection;

    @FXML
    private ComboBox<String> affectedTargets;

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
    private Label processingTimeLabel;

    @FXML
    private Label limitedPermanentLabel;

    @FXML
    private Label successRateLabel;

    @FXML
    private TextField processingTimeTextField;

    @FXML
    private TextArea logTextArea;

    @FXML
    private Label successRateWithWarnings;

    @FXML
    private Spinner<?> processingTimeSpinner;

    @FXML
    private RadioButton limitedRadioButton;

    @FXML
    private ToggleGroup limitedOrPermanent;

    @FXML
    private ImageView graphImage;

    @FXML
    private RadioButton permanentRadioButton;

    @FXML
    private Slider successRateSlider;

    @FXML
    private Slider successRateWithWarningsSlider;

    @FXML
    private Tab logPane;

    @FXML
    private TextField successWithWarningRateText;

    @FXML
    private TextField successRateText;

    @FXML
    private Button ApplyParametersButton;

    @FXML
    void affectedTargetsPressed(ActionEvent event) {

    }

    @FXML
    private Label currentSelectedTargetLabel;

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

        for (Target target : graph.getGraphTargets().values())
            targetSet.add(target.getTargetName());

        this.executor = Executors.newFixedThreadPool(parallelThreads);
        TaskThread taskThread = new TaskThread(graph, TaskThread.TaskType.Simulation, taskParametersMap, new GraphSummary(graph, null),
                targetSet, executor, logTextArea);
        taskThread.start();
    }
    @FXML
    void getProcessingTime(ActionEvent event) {
//        long timeInMS = -1;
//        timeInMS = Integer.parseInt(this.processingTimeTextField.getText());
//        processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);
//        getSimulationTaskParametersFromUser();
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
    void targetSelectionPressed(ActionEvent event) {}

    @FXML
    void taskSelectionPressed(ActionEvent event) {

        if(!taskSelection.getSelectionModel().isEmpty())
        {
            setForSimulationTask(!taskSelection.getValue().equals("Simulation"));
            enableAffectedButtons();
        }
    }

    private void setForSimulationTask(boolean flag) {

        this.processingTimeLabel.setDisable(flag);
        this.limitedPermanentLabel.setDisable(flag);
        this.successRateLabel.setDisable(flag);
        this.successRateWithWarnings.setDisable(flag);

        this.processingTimeTextField.setDisable(flag);
        this.limitedRadioButton.setDisable(flag);
        this.permanentRadioButton.setDisable(flag);

        this.successRateSlider.setDisable(flag);
        this.successRateWithWarningsSlider.setDisable(flag);

        this.successRateText.setDisable(flag);
        this.successWithWarningRateText.setDisable(flag);
        this.ApplyParametersButton.setDisable(flag);

        addListenersForSliders();
        addListenersForTextFields();
    }

    private void addListenersForTextFields() {
        this.successRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            successRateSlider.setValue(Double.parseDouble(newValue));
        });

        this.successWithWarningRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            successRateWithWarningsSlider.setValue(Double.parseDouble(newValue));
        });
    }

    private void addListenersForSliders() {

        this.successRateSlider.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {

                successRateText.setText(String.format("%.3f", newValue));
            }
        });

        this.successRateWithWarningsSlider.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
            {
                successWithWarningRateText.setText(String.format("%.3f", newValue));
            }
        });
    }

    private void enableAffectedButtons() {

        this.targetSelection.setDisable(false);
        this.affectedTargets.setDisable(false);
        this.currentSelectedTargetLabel.setDisable(false);
        this.currentSelectedTargetListView.setDisable(false);
    }

    public void setGraphImage(String fullFileName) throws FileNotFoundException
    {
        InputStream stream = new FileInputStream(fullFileName);
        Image image = new Image(stream);

        graphImage.setImage(image);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        setAllTargetsList();
    }

    private void applyTaskParametersForAllTargets(TaskParameters taskParameters)
    {
        taskParametersMap = new HashMap<>();
        for(Target target : graph.getGraphTargets().values())
            taskParametersMap.put(target.getTargetName(), taskParameters);
    }

//    private TaskParameters getTaskParametersFromUser()
//    {
////        Scanner scanner = new Scanner(System.in);
//        Duration processingTime = null;
//        long timeInMS = -1;
//        Boolean isRandom = true;
//        Double successRate = -1.0, successWithWarnings = -1.0;
//        TaskParameters taskParameters = new TaskParameters();
////
////        System.out.print("Enter the processing time (in m/s) for each task: ");
////        timeInMS = scanner.nextLong();
////        processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);
////
////        System.out.print("Choose if the processing time is limited by the value you just entered, or permanent (0 - limited, 1 - permanent): ");
////        int temp = scanner.nextInt();
////        isRandom = temp == 0;
////
////        System.out.print("Enter the success rate of the task (value between 0 and 1): ");
////        successRate = scanner.nextDouble();
////
////        System.out.print("If the task ended successfully, what is the chance that it ended with warnings? (value between 0 and 1): ");
////        successWithWarnings = scanner.nextDouble();
//
//        timeInMS = 5000;
//        processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);
//        isRandom = true;
//        successRate = 0.7;
//        successWithWarnings = 0.3;
//
//        taskParameters.setProcessingTime(processingTime);
//        taskParameters.setRandom(isRandom);
//        taskParameters.setSuccessRate(successRate);
//        taskParameters.setSuccessWithWarnings(successWithWarnings);
//
//        return taskParameters;
//    }

    private void setAllTargetsList()
    {
        int i = 0;
        ObservableList<String> allTargetsList = FXCollections.observableArrayList();

        for(Target currentTargetName : graph.getGraphTargets().values())
            allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = allTargetsList.sorted();
        targetSelection.getItems().addAll(sorted);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        ObservableList<String> taskSelectionList = FXCollections.observableArrayList("Simulation","Compilation");
        taskSelection.setItems(taskSelectionList);
    }


    public TaskParameters getSimulationTaskParametersFromUser()
    {
        TaskParameters taskParameters = new TaskParameters();
        Duration processingTime = null;
        long timeInMS = -1;
        Boolean isRandom = true;
        Double successRate = -1.0, successWithWarnings = -1.0;

        try {
            timeInMS = Integer.parseInt(processingTimeTextField.getText());
            processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);

            isRandom = !this.permanentRadioButton.isSelected();
            successRate = this.successRateSlider.getValue();
            successWithWarnings = this.successRateWithWarningsSlider.getValue();

            taskParameters.setProcessingTime(processingTime);
            taskParameters.setRandom(isRandom);
            taskParameters.setSuccessRate(successRate);
            taskParameters.setSuccessWithWarnings(successWithWarnings);
        }
        catch(Exception ex)
        {
            ErrorPopup(ex, "Invalid Parameters");
        }

        return taskParameters;
    }

    public void setParallelThreads(int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    @FXML
    void ApplyParametersToTask(ActionEvent event)
    {
        TaskParameters taskParameters = getSimulationTaskParametersFromUser();
        applyTaskParametersForAllTargets(taskParameters);
    }

    private void ErrorPopup(Exception ex, String title)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }
}
