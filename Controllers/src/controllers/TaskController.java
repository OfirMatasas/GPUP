package controllers;

import information.TaskTargetInformation;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;
import task.TaskParameters;
import task.TaskThread;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
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
    private Thread updateThread;
    private GraphSummary graphSummary;
    private ObservableList<String> allTargetsList;
    private TaskThreadWatcher taskThreadWatcher;
    private int finishedTargets = 0;
    private Task<Void> task;

    public class TaskThreadWatcher extends Thread
    {
        @Override
        public void run()
        {
            disableTaskOptions(true);
            PauseButton.setDisable(false);
            stopButton.setDisable(false);

            while(taskThread.isAlive())
            {
                if(taskThread.getStatusChanged())
                    taskPausedOrStopped();
            }

            disableTaskOptions(false);
            PauseButton.setDisable(true);
            stopButton.setDisable(true);
            Platform.runLater(() -> PauseButton.setText("Pause"));
        }

        public void taskPausedOrStopped()
        {
            if(taskThread.getStopped()) //Stopped
            {
                if(!taskThread.getPaused())
                    logTextArea.appendText("\nWaiting for the task to stop...\n\n");

                while(!executor.isTerminated()) {}

                Platform.runLater(() -> logTextArea.appendText("\nTask stopped!\n\n"));
            }
            else //Paused / Resumed
            {
                String firstOutput, secondOutput = "", newButtonText;

                if(taskThread.getPaused()) //Paused
                {
                    firstOutput = "\nWaiting for the task to pause...\n\n";
                    newButtonText = "Resume";
                    secondOutput = "\nTask paused!\n\n";
                }
                else //Resumed
                {
                    firstOutput = "\nTask resumed!\n\n";
                    newButtonText = "Pause";
                }

                PauseButton.setDisable(true);
                stopButton.setDisable(true);
                logTextArea.appendText(firstOutput);

                if(taskThread.getPaused())
                    while(!executor.isTerminated()) {}

                String finalSecondOutput = secondOutput;
                Platform.runLater(() ->
                        {
                            logTextArea.appendText(finalSecondOutput);
                            PauseButton.setText(newButtonText);
                        });
                PauseButton.setDisable(false);
                stopButton.setDisable(false);
            }

            taskThread.resetStatusChanged();
        }
    }

    @FXML
    private BorderPane taskBorderPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private ComboBox<String> TaskTargetSelection;

    @FXML
    private HBox toolBarHBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label targetsFinishedLabel;

    @FXML
    private Label progressBarLabel;

    @FXML
    private Button addSelectedButton;

    @FXML
    private Button removeSelectedButton;

    @FXML
    private Button clearTableButton;

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
    private TableColumn<TaskTargetInformation, String> resultStatusColumn;

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
    void removeSelectedRowFromTable(ActionEvent event)
    {
        TaskTargetInformation chosenTarget = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
        int index = chosenTarget.getNumber() - 1, size = this.taskTargetDetailsTableView.getItems().size();

        this.taskTargetDetailsTableView.getItems().remove(chosenTarget);

        while(size - 1 > index)
        {
            chosenTarget = this.taskTargetDetailsTableView.getItems().get(index);
            chosenTarget.setNumber(++index);
        }

        updateTargetTaskDetailsInTextArea();
        turnOnIncrementalButton();
    }

    private void updateTargetTaskDetailsInTextArea() {
        if(!taskTargetDetailsTableView.getItems().isEmpty())
        {
            TaskTargetInformation taskTargetInformation = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
            showDetailsOfSelectedTargetInTextArea(taskTargetInformation);
        }
        else
            enableTargetInfoTextArea(false);
    }

    @FXML
    void ClearTable(ActionEvent event)
    {
        taskTargetDetailsTableView.getItems().clear();
        incrementalRadioButton.setDisable(true);
    }

    @FXML
    void addSelectedTargetsToTable(ActionEvent event)
    {
        setTaskTargetDetailsTable();
        turnOnIncrementalButton();
    }

    private void turnOnIncrementalButton() {
        boolean change = false;

        for(TaskTargetInformation curr : taskTargetDetailsTableView.getItems())
        {
            if(curr.getResultStatus().equals("Undefined"))
            {
                change = true;
                break;
            }
        }
        incrementalRadioButton.setDisable(change);
    }

    @FXML
    public void initialize()
    {
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
        this.currentSelectedTargets.clear();
    }

    @FXML
    void graphViewTabPressed(Event event) {}

    @FXML
    void limitedOptionPressed(ActionEvent event) {}

    @FXML
    void logViewTabPressed(Event event) {}

    @FXML
    void pausePressed(ActionEvent event) {
        if(!this.taskThread.getPaused()) //Pausing the task
            this.taskThread.pauseTheTask();
        else //Resuming the task
            this.taskThread.continueTheTask();
    }

    @FXML
    void permanentOptionPressed(ActionEvent event) {}

    @FXML
    void runPressed(ActionEvent event) throws FileNotFoundException, OpeningFileCrash {
        if(!checkForValidRun())
            return;

        this.updateThread = new Thread(this::updateTableRuntimeStatuses);
        this.taskThreadWatcher = new TaskThreadWatcher();

        Set<String> currentRunTargets = setCurrentRunTargets();
        applyTaskParametersForAllTargets(taskParameters);
        turnOnProgressBar();

        this.executor = Executors.newFixedThreadPool(parallelThreads);

        this.taskThread = new TaskThread(this.graph, TaskThread.TaskType.Simulation, this.taskParametersMap, this.graphSummary,
                currentRunTargets, this.executor, this.parallelThreads, this.logTextArea, this.incrementalRadioButton.isSelected());

        this.taskThreadWatcher.setDaemon(true);
        this.taskThread.start();
        this.taskThreadWatcher.start();
        this.updateThread.start();

        taskThread.start();
        createNewProgressBar();
        taskThreadWatcher.start();

        updateThread.start();


        if(!this.firstRun)
        {
            this.firstRun = false;
            this.lastRunTargets.clear();
        }

        this.lastRunTargets.addAll(currentRunTargets);
    }

    private void createNewProgressBar()
    {

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxSize = taskTargetDetailsTableView.getItems().size();
                while(taskThread.isAlive())
                {
                    Thread.sleep(200);
                    getFinishedTargetsInRealTime();
                    updateProgress(finishedTargets,maxSize);
                }
                updateProgress(maxSize,maxSize);
                return null;
            }
        };
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressBarLabel.textProperty().bind( new ObservableValue<String>() {
            @Override
            public String getValue() {
                return String.valueOf(task.progressProperty().getValue() * 100) + "%";
            }
        });
        Thread progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);
        progressBarThread.start();
    }

    private void turnOnProgressBar() {
        this.progressBar.setDisable(false);
        this.progressBarLabel.setDisable(false);
        this.targetsFinishedLabel.setDisable(false);
    }

    private Set<String> setCurrentRunTargets() {
        Set<String> currentRunTargets = new HashSet<>();

        for(TaskTargetInformation curr : taskTargetDetailsTableView.getItems())
            currentRunTargets.add(curr.getTargetName());

        return currentRunTargets;
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
        currentSelectedTargets.clear();
        graph.getGraphTargets().values().forEach(targetName -> currentSelectedTargets.add(targetName.getTargetName()));

//        selectAllButton.setDisable(true);
//        deselectAllButton.setDisable(false);
    }

    @FXML
    void stopPressed(ActionEvent event) {
        taskThread.stopTheTask();
    }

    @FXML
    void tableViewTabPressed(Event event) {}

    @FXML
    void targetSelectionPressed(ActionEvent event) {
        affectedTargets.setDisable(false);

        currentSelectedTargets.clear();
        currentSelectedTargets.add(targetSelection.getValue());

        if(affectedTargets.getValue() != null)
            affectedTargetsPressed(event);

//        deselectAllButton.setDisable(false);
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
            try {
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
            }
            catch (Exception ex)
            {
                ShowPopup("Invalid input in task parameters!", "Invalid input", Alert.AlertType.ERROR);
                successRateText.clear();
            }
        });

        this.successWithWarningRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
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
                }
                catch (Exception ex) {
                    ShowPopup("Invalid input in task parameters!", "Invalid input", Alert.AlertType.ERROR);
                    successWithWarningRateText.clear();
                }
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

        //Random time for each target
        for(Target target : graph.getGraphTargets().values())
        {
            processingTime = taskParameters.getProcessingTime();
            randomTime = (long)(Math.random() * (processingTime.toMillis())) + 1;
            processingTime = (Duration.of(randomTime, ChronoUnit.MILLIS));

            currTaskParameters = new TaskParameters(processingTime, isRandom, successRate, successRateWithWarnings);
            taskParametersMap.put(target.getTargetName(), currTaskParameters);
        }
    }

    private void setAllTargetsList() {
        int i = 0;
        allTargetsList = FXCollections.observableArrayList();

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
        addListenersToButtons();

        affectedTargetsOptions.addAll(NONE, DEPENDED, REQUIRED);
        affectedTargets.setItems(affectedTargetsOptions);

        initializeGraphDetails();
    }

    private void addListenersForSelectedTargets() {
        //Enable/Disable incremental, selectAll, deselectAll button
        currentSelectedTargets.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                boolean containAll = currentSelectedTargets.containsAll(allTargetsList);
                selectAllButton.setDisable(containAll);
                deselectAllButton.setDisable(!containAll);

                while (c.next()) {
                    for (String remitem : c.getRemoved()) {
                        currentSelectedTargetListView.getItems().remove(remitem);
                        addSelectedButton.setDisable(true);
                    }
                    for (String additem : c.getAddedSubList()) {
                        currentSelectedTargetListView.getItems().add(additem);
                        addSelectedButton.setDisable(false);
                    }
                }
            }
        });
    }

    private void addListenersToButtons()
    {
        taskTargetDetailsTableView.getItems().addListener(new ListChangeListener<TaskTargetInformation>() {
            @Override
            public void onChanged(Change<? extends TaskTargetInformation> c) {
                removeSelectedButton.setDisable(c.getList().isEmpty());
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
            this.runButton.setDisable(false);
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
        this.resultStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("resultStatus"));
    }

    private void setTaskTargetDetailsTable()
    {
        int i = taskTargetDetailsTableView.getItems().size() + 1;
        String targetPosition, targetRuntimeStatus, targetResultStatus;
        TaskTargetInformation taskTargetInformation;
        ObservableList<TaskTargetInformation> tableList = taskTargetDetailsTableView.getItems();

        for(String currentTarget : currentSelectedTargetListView.getItems())
        {
            if(!targetExistedInTable(tableList, currentTarget))
            {
                targetPosition = graph.getTarget(currentTarget).getTargetPosition().toString();
                targetRuntimeStatus = graphSummary.getTargetsSummaryMap().get(currentTarget).getRuntimeStatus().toString();
                targetResultStatus = graphSummary.getTargetsSummaryMap().get(currentTarget).getResultStatus().toString();
                taskTargetInformation = new TaskTargetInformation(i++, currentTarget, targetPosition, targetRuntimeStatus, targetResultStatus);

                taskTargetDetailsList.add(taskTargetInformation);
            }
        }
        taskTargetDetailsTableView.setItems(taskTargetDetailsList);
    }

    private boolean targetExistedInTable(ObservableList<TaskTargetInformation> tableList, String currentTargetName) {
        for(TaskTargetInformation currInfo : tableList)
        {
            if(currInfo.getTargetName().equals(currentTargetName))
                return true;
        }
        return false;
    }

    private void updateTableRuntimeStatuses()
    {
        ObservableList<TaskTargetInformation> itemsList = taskTargetDetailsTableView.getItems();
        LocalTime startTime = LocalTime.now();
        LocalTime currTime = LocalTime.now();

        while (taskThread.isAlive())
        {
            startTime = LocalTime.now();
            updateTable(itemsList, startTime, currTime);

        }
        updateTable(itemsList, startTime, currTime);
    }

    public void updateTable(ObservableList<TaskTargetInformation> itemsList , LocalTime startTime, LocalTime currTime)
    {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (TaskTargetInformation item : itemsList)
        {
            item.setCurrentRuntimeStatus(graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getRuntimeStatus().toString());
            item.setResultStatus(graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getResultStatus().toString());
        }
        Platform.runLater(()->{this.taskTargetDetailsTableView.refresh();});
    }


    public void getSelectedRow(MouseEvent mouseEvent)
    {
        updateTargetTaskDetailsInTextArea();
        enableTargetInfoTextArea(true);
        removeSelectedButton.setDisable(false);
    }

    private void enableTargetInfoTextArea(boolean flag) {
        taskDetailsOnTargetTextArea.setVisible(flag);
        taskDetailsOnTargetTextArea.setDisable(!flag);
    }

    public void showDetailsOfSelectedTargetInTextArea(TaskTargetInformation taskTargetInformation)
    {
        String detailMsg = null;
        String currentTargetName = taskTargetInformation.getTargetName();
        TargetSummary currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTargetName);
        if(currentTargetName!=null) {
            Target currentTarget = graph.getTarget(currentTargetName);
             detailMsg = "Target : " + currentTargetName + "\n"
                    + "Position : " + currentTarget.getTargetPosition() + "\n";

            if (currentTarget.getSerialSets().isEmpty())
                detailMsg += "Serial Sets : None" + "\n";
            else
                detailMsg += "Serial Sets : " + currentTarget.getSerialSets() + "\n";

            switch (currentTargetSummary.getRuntimeStatus())
            {
                case Frozen:
                {
                    detailMsg += "List of dependencies that the target " + currentTargetName + " is waiting for to finish : ";
                    if(printTargetWaitingForTargets(currentTargetName).isEmpty())
                        detailMsg += "none.";
                    else
                        detailMsg += printTargetWaitingForTargets(currentTargetName);
                    break;
                }
                case Skipped:
                {
                    detailMsg += "Target's runtime status : Skipped \n";
                    detailMsg += "List of dependencies that their process failed are : ";
                    if(printProcessedFailedTargets(currentTargetName).isEmpty())
                        detailMsg += "none.";
                    else
                        detailMsg += printProcessedFailedTargets(currentTargetName);
                    break;
                }
                case Waiting:
                {
                    detailMsg += "The target " + currentTargetName + " is waiting for : " + currentTargetSummary.currentWaitingTime().toMillis() + " M\\S";
                    break;
                }
                case InProcess:
                {
                    detailMsg += "The target " + currentTargetName + " is in process for : " + currentTargetSummary.currentProcessingTime().toMillis() + " M\\S";
                    break;
                }
                case Finished:
                {
                    Duration time = currentTargetSummary.getTime();
                    detailMsg += "Target's result status : ";

                    if(currentTargetSummary.isSkipped())
                        detailMsg += "Skipped\n";
                    else
                        detailMsg += currentTargetSummary.getResultStatus() + "\n";

                    if(!currentTargetSummary.isSkipped())
                        detailMsg += String.format("Target's running time : %02d:%02d:%02d\n", time.toHours(), time.toMinutes(), time.getSeconds()) + "\n";
                    break;
                }

            }
        }

        this.taskDetailsOnTargetTextArea.setText(detailMsg);
    }

    public String printTargetWaitingForTargets(String currentTargetName)
    {
        String waitingForTargets = "";
       for(String dependedOnTarget : graph.getTarget(currentTargetName).getAllDependsOnTargets())
       {
            if(!lastRunTargets.contains(dependedOnTarget))
                continue;
            else
            {
                if(!graphSummary.getTargetsSummaryMap().get(dependedOnTarget).getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished))
                    waitingForTargets = waitingForTargets + dependedOnTarget + " ";
            }
       }
       return waitingForTargets;
    }

    public String printProcessedFailedTargets(String currentTargetName)
    {
        String processedFailedTargets = "";
        for(String dependedOnTarget : graph.getTarget(currentTargetName).getAllDependsOnTargets())
        {
            if(!lastRunTargets.contains(dependedOnTarget))
                continue;
            else
            {
                if(graphSummary.getTargetsSummaryMap().get(dependedOnTarget).getResultStatus().equals(TargetSummary.ResultStatus.Failure))
                    processedFailedTargets = processedFailedTargets + dependedOnTarget + " ";
            }
        }
        return processedFailedTargets;
    }

    public void getFinishedTargetsInRealTime()
    {
        finishedTargets = 0;
        for(TaskTargetInformation currItem : taskTargetDetailsTableView.getItems())
        {
            if(currItem.getCurrentRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished.toString())||currItem.getCurrentRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped.toString()))
                finishedTargets++;
        }
    }


}
