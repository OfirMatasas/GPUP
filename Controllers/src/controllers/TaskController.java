package controllers;

import information.TaskTargetInformation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLOutput;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskController implements Initializable {
    private Graph graph;
    private Map<String, TaskParameters> taskParametersMap = new HashMap<>();
    private int parallelThreads;
    private ExecutorService executor;
    private TaskParameters taskParameters;
    private final ObservableList<String> affectedTargetsOptions = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedTargets = FXCollections.observableArrayList();
    private final String NONE = "none";
    private final String REQUIRED = "All required-for targets";
    private final String DEPENDED = "All depends-on targets";
    private final ObservableList<TaskTargetInformation> taskTargetDetailsList = FXCollections.observableArrayList();
    private Set<String> lastRunTargets = new HashSet<>();
    private Boolean firstRun = true;
    private TaskThread taskThread;
    private Thread updateThread = new Thread(this::updateTableRuntimeStatuses);
    private GraphSummary graphSummary;

    public class TaskThreadWatcher extends Thread
    {
        @Override
        public void run()
        {
            disableTaskOptions(true);

            while(taskThread.isAlive()) {}

            disableTaskOptions(false);
        }
    }

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
    private ListView<String> currentSelectedTargetListView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tableViewTabPane;

    @FXML
    private TableView<TaskTargetInformation> taskTargetDetailsTableView;

    @FXML
    private TableColumn<TaskTargetInformation, Integer> numberColumn;

    @FXML
    private TableColumn<TaskTargetInformation, String> targetNameColumn;

    @FXML
    private TableColumn<TaskTargetInformation, String> positionColumn;

    @FXML
    private TableColumn<TaskTargetInformation, String> currentRuntimeStatusColumn;

    @FXML
    private Tab graphViewTabPane;

    @FXML
    private Tab graphViewTabPane1;

    @FXML
    private Pane footerPane;

    @FXML
    private RadioButton fromScratchRadioButton;

    @FXML
    private ToggleGroup scratchOrIncremental;

    @FXML
    private RadioButton incrementalRadioButton;

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
    private TextArea taskDetailsOnTargetTextArea;

    @FXML
    public void initialize() {
        initializeGraphDetails();

    }

    @FXML
    void affectedTargetsPressed(ActionEvent event) {
        Set<String> affectedTargetsSet = null;

        switch (affectedTargets.getValue())
        {
            case DEPENDED:
            {
                affectedTargetsSet = graph.getTarget(targetSelection.getValue()).getAllDependsOnTargets();
                break;
            }
            case REQUIRED:
            {
                affectedTargetsSet = graph.getTarget(targetSelection.getValue()).getAllRequiredForTargets();
                break;
            }
            default:
                break;
        }

        if(affectedTargetsSet != null)
        {
            currentSelectedTargets.clear();
            currentSelectedTargets.add(targetSelection.getValue());
            currentSelectedTargets.addAll(affectedTargetsSet);

            incrementalRadioButton.setDisable(!lastRunTargets.containsAll(currentSelectedTargets));
        }
    }

    @FXML
    private Label currentSelectedTargetLabel;

    @FXML
    void deselectAllPressed(ActionEvent event) {
        currentSelectedTargets.clear();

        deselectAllButton.setDisable(true);
        selectAllButton.setDisable(false);
    }

    @FXML
    void graphViewTabPressed(Event event) {}

    @FXML
    void limitedOptionPressed(ActionEvent event) {}

    @FXML
    void logViewTabPressed(Event event) {}

    @FXML
    void pausePressed(ActionEvent event) {}

    @FXML
    void permanentOptionPressed(ActionEvent event) {}

    @FXML
    void runPressed(ActionEvent event) throws FileNotFoundException, OpeningFileCrash {
        if(!checkForValidRun())
            return;

        Set<String> currentRunTarget = new HashSet<>(currentSelectedTargetListView.getItems());
        TaskThreadWatcher taskThreadWatcher = new TaskThreadWatcher();

        applyTaskParametersForAllTargets(taskParameters);
        this.taskDetailsOnTargetTextArea.setDisable(false);
        this.executor = Executors.newFixedThreadPool(parallelThreads);

        taskThread = new TaskThread(graph, TaskThread.TaskType.Simulation, taskParametersMap, graphSummary,
                currentRunTarget, executor, logTextArea, ()->{updateThread.interrupt();});

        taskThreadWatcher.setDaemon(true);

        taskThread.start();
        taskThreadWatcher.start();

        if(!this.firstRun)
        {
            firstRun = false;
            lastRunTargets.clear();
        }

        lastRunTargets.addAll(currentRunTarget);

    }

    private Boolean checkForValidRun()
    {
        String errorMessage = "";

        if(taskParameters == null)
            errorMessage = "You have to apply the parameters for the task first!";


        if(!errorMessage.equals(""))
        {
            ShowPopup(errorMessage, "Can't start task", Alert.AlertType.ERROR);
            return false;
        }
        return true;
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
        graph.getGraphTargets().values().forEach(targetName -> currentSelectedTargets.add(targetName.getTargetName()));

        selectAllButton.setDisable(true);
        deselectAllButton.setDisable(false);
    }

    @FXML
    void stopPressed(ActionEvent event) {}

    @FXML
    void tableViewTabPressed(Event event) {}

    @FXML
    void targetSelectionPressed(ActionEvent event) {
        affectedTargets.setDisable(false);

        currentSelectedTargets.clear();
        currentSelectedTargets.add(targetSelection.getValue());

        if(affectedTargets.getValue() != null)
            affectedTargetsPressed(event);

        deselectAllButton.setDisable(false);
    }

    @FXML
    void taskSelectionPressed(ActionEvent event) {

        if(!taskSelection.getSelectionModel().isEmpty())
        {
            setForSimulationTask(!taskSelection.getValue().equals("Simulation"));
            disableButtons(false);
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
    }

    private void addListenersForTextFields() {
        this.successRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(""))
                return;

            if(Double.parseDouble(newValue) > 1.0)
            {
                successRateText.setText(String.valueOf(1.0));
                successRateSlider.setValue(1.0);
            }
            else if(Double.parseDouble(newValue) < 0.0)
            {
                successRateText.setText(String.valueOf(0.0));
                successRateSlider.setValue(0.0);
            }
            else
                successRateSlider.setValue(Double.parseDouble(newValue));
        });

        this.successWithWarningRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(""))
                return;

            if(Double.parseDouble(newValue) > 1.0)
            {
                successRateWithWarningsSlider.setValue(1.0);
                successWithWarningRateText.setText(String.valueOf(1.0));
            }
            else if(Double.parseDouble(newValue) < 0.0)
            {
                successRateWithWarningsSlider.setValue(0.0);
                successWithWarningRateText.setText(String.valueOf(0.0));
            }
            else
                successRateWithWarningsSlider.setValue(Double.parseDouble(newValue));
        });
    }

    private void addListenersForSliders() {
        this.successRateSlider.valueProperty().addListener((observable, oldValue, newValue) -> successRateText.setText(String.format("%.3f", newValue)));
        this.successRateWithWarningsSlider.valueProperty().addListener((observable, oldValue, newValue) -> successWithWarningRateText.setText(String.format("%.3f", newValue)));
    }

    private void disableButtons(Boolean flag) {

        this.targetSelection.setDisable(flag);
        this.affectedTargets.setDisable(flag);
        this.currentSelectedTargetLabel.setDisable(flag);
        this.currentSelectedTargetListView.setDisable(flag);

        this.fromScratchRadioButton.setDisable(flag);

        this.selectAllButton.setDisable(flag);
    }

    private void disableTaskOptions(Boolean flag)
    {
        this.runButton.setDisable(flag);

        this.currentSelectedTargetLabel.setDisable(flag);
        this.taskSelection.setDisable(flag);
        this.targetSelection.setDisable(flag);
        this.affectedTargets.setDisable(flag);

        this.currentSelectedTargetListView.setDisable(flag);
        this.selectAllButton.setDisable(flag);
        this.deselectAllButton.setDisable(flag);

        this.fromScratchRadioButton.setDisable(flag);
        this.incrementalRadioButton.setDisable(flag);

        setForSimulationTask(flag);

//        this.processingTimeLabel.setDisable(flag);
//        this.processingTimeTextField.setDisable(flag);
//        this.limitedPermanentLabel.setDisable(flag);
//        this.limitedRadioButton.setDisable(flag);
//        this.permanentRadioButton.setDisable(flag);
//        this.successRateLabel.setDisable(flag);
//        this.successRateSlider.setDisable(flag);
//        this.successRateText.setDisable(flag);
//        this.successRateWithWarnings.setDisable(flag);
//        this.successWithWarningRateText.setDisable(flag);
//        this.successRateWithWarningsSlider.setDisable(flag);
//        this.ApplyParametersButton.setDisable(flag);
    }

    public void setGraphImage(String fullFileName) throws FileNotFoundException {
        InputStream stream = new FileInputStream(fullFileName);
        Image image = new Image(stream);

        graphImage.setImage(image);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        this.graphSummary = new GraphSummary(graph,null);
        setAllTargetsList();
        setTaskTargetDetailsTable();

    }

    private void applyTaskParametersForAllTargets(TaskParameters taskParameters) {
        taskParametersMap = new HashMap<>();
        TaskParameters currTaskParameters;
        Duration processingTime;
        long randomTime;
        double successRate = taskParameters.getSuccessRate(), successRateWithWarnings = taskParameters.getSuccessWithWarnings();
        Boolean isRandom = taskParameters.isRandom();

        //permanent time for all targets
        if(!isRandom)
        {
            for(Target target : graph.getGraphTargets().values())
                taskParametersMap.put(target.getTargetName(), taskParameters);

            return;
        }

        //Random time for each target`
        for(Target target : graph.getGraphTargets().values())
        {
            processingTime = taskParameters.getProcessingTime();
            randomTime = (long)(Math.random() * (processingTime.toMillis())) + 1;
            processingTime = (Duration.of(randomTime, ChronoUnit.MILLIS));

            currTaskParameters = new TaskParameters(processingTime, isRandom, successRate, successRateWithWarnings);
            taskParametersMap.put(target.getTargetName(), currTaskParameters);
        }
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

    private void setAllTargetsList() {
        int i = 0;
        ObservableList<String> allTargetsList = FXCollections.observableArrayList();

        for(Target currentTargetName : graph.getGraphTargets().values())
            allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = allTargetsList.sorted();
        targetSelection.getItems().addAll(sorted);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> taskSelectionList = FXCollections.observableArrayList("Simulation", "Compilation");
        taskSelection.setItems(taskSelectionList);

        addListenersForSliders();
        addListenersForTextFields();
        addListenersForSelectedTargets();

        affectedTargetsOptions.addAll(NONE, DEPENDED, REQUIRED);
        affectedTargets.setItems(affectedTargetsOptions);
        initializeGraphDetails();

    private void addListenersForSelectedTargets() {
        //Enable/Disable incremental button
        currentSelectedTargets.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                incrementalRadioButton.setDisable(!lastRunTargets.containsAll(currentSelectedTargets));
            }
        });

        //Change listview according to chosen targets
        currentSelectedTargets.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                while (c.next()) {
                    for (String remitem : c.getRemoved()) {
                        currentSelectedTargetListView.getItems().remove(remitem);
                    }
                    for (String additem : c.getAddedSubList()) {
                        currentSelectedTargetListView.getItems().add(additem);
                    }
                }
            }
        });
    }

    public TaskParameters getSimulationTaskParametersFromUser() {
        TaskParameters taskParameters = new TaskParameters();
        Duration processingTime;
        long timeInMS;
        Boolean isRandom;
        Double successRate, successWithWarnings;

        try {
            timeInMS = Integer.parseInt(processingTimeTextField.getText());
            processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);

            isRandom = this.limitedRadioButton.isSelected();
            successRate = this.successRateSlider.getValue();
            successWithWarnings = this.successRateWithWarningsSlider.getValue();

            taskParameters.setProcessingTime(processingTime);
            taskParameters.setRandom(isRandom);
            taskParameters.setSuccessRate(successRate);
            taskParameters.setSuccessWithWarnings(successWithWarnings);
        }
        catch(Exception ex)
        {
            ShowPopup("Invalid input in parameters.", "Invalid Parameters", Alert.AlertType.ERROR);
        }

        return taskParameters;
    }

    public void setParallelThreads(int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    @FXML
    void ApplyParametersToTask(ActionEvent event) {
        this.taskParameters = getSimulationTaskParametersFromUser();

        if(this.taskParameters.getProcessingTime()!=null) // Checking only on the processing time, because other parameters are already initialized
           setToolBarButtonsEnable();
    }

    private void setToolBarButtonsEnable()
    {
        this.runButton.setDisable(false);
        this.PauseButton.setDisable(false);
        this.stopButton.setDisable(false);
    }

    private void ShowPopup(String message, String title, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void fromScratchOptionPressed(ActionEvent event) {

    }

    @FXML
    void incrementalOptionPressed(ActionEvent event) {

    }

    //-----------------------------------------------------------------------------------------------

    private void initializeGraphDetails() {
        this.numberColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, Integer>("number"));
        this.targetNameColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("targetName"));
        this.positionColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("position"));
        this.currentRuntimeStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("currentRuntimeStatus"));
    }

    private void setTaskTargetDetailsTable() {
        int i = 1;
        TaskTargetInformation taskTargetInformation;
        for (Target currentTarget : graph.getGraphTargets().values())
        {
              taskTargetInformation = new TaskTargetInformation(i,currentTarget.getTargetName(),
                      currentTarget.getTargetPosition().toString(),graphSummary.getTargetsSummaryMap().get(currentTarget.getTargetName()).getRuntimeStatus().toString());
              taskTargetDetailsList.add(taskTargetInformation);
            ++i;
        }
        taskTargetDetailsTableView.setItems(taskTargetDetailsList);
        updateThread.start();
    }

    private void updateTableRuntimeStatuses()
    {
        ObservableList<TaskTargetInformation> itemsList = taskTargetDetailsTableView.getItems();
        LocalTime startTime;
        LocalTime currTime;
        startTime = LocalTime.now();
        currTime = LocalTime.now();

        while (!updateThread.isInterrupted())
        {
            currTime=LocalTime.now();
            while (currTime.compareTo(startTime.plusSeconds(1)) < 0)
            {
                currTime = LocalTime.now();
            }
            startTime = LocalTime.now();
            for (TaskTargetInformation item : itemsList)
            {
                item.setCurrentRuntimeStatus(graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getRuntimeStatus().toString());
            }
            Platform.runLater(()->{this.taskTargetDetailsTableView.refresh();});
        }
    }

}
