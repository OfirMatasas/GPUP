package controllers;

import information.TaskTargetInformation;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;
import task.CompilationParameters;
import task.SimulationParameters;
import task.TaskOutput;
import task.TaskThread;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TaskControlController implements Initializable {
    private Graph graph;
    private Map<String, SimulationParameters> taskParametersMap = new HashMap<>();
    private int maxParallelThreads;
    private SimulationParameters taskParameters;
    private final ObservableList<String> affectedTargetsOptions = FXCollections.observableArrayList();
    private final ObservableList<String> currentSelectedTargets = FXCollections.observableArrayList();
    private final String REQUIRED = "All required-for targets";
    private final String DEPENDED = "All depends-on targets";
    private final String SIMULATION ="Simulation";
    private final String COMPILATION ="Compilation";
    private final ObservableList<TaskTargetInformation> taskTargetDetailsList = FXCollections.observableArrayList();
    private TaskThread taskThread;
    private GraphSummary graphSummary;
    private ObservableList<String> allTargetsList;
    private int finishedTargets;
    private int numOfThreads;
    private File sourceCodeDirectory = null;
    private File outputDirectory = null;
    private TaskThread.TaskType taskType;

    public class TaskThreadWatcher extends Thread
    {
        @Override
        public void run()
        {
            disableTaskOptions(true);
            TaskControlController.this.PauseButton.setDisable(false);
            TaskControlController.this.stopButton.setDisable(false);

            while(TaskControlController.this.taskThread.isAlive())
            {
                if(TaskControlController.this.taskThread.getStatusChanged())
                    taskPausedOrStopped();
            }

            disableTaskOptions(false);
            TaskControlController.this.PauseButton.setDisable(true);
            TaskControlController.this.stopButton.setDisable(true);

            Platform.runLater(() -> TaskControlController.this.PauseButton.setText("Pause"));
        }

        public void taskPausedOrStopped()
        {
            if(TaskControlController.this.taskThread.getStopped()) //Stopped
            {
                if(!TaskControlController.this.taskThread.getPaused())
                    TaskControlController.this.logTextArea.appendText("\nWaiting for the task to stop...\n\n");

                while(!TaskControlController.this.taskThread.getExecutor().isTerminated()) {}

                Platform.runLater(() -> TaskControlController.this.logTextArea.appendText("\nTask stopped!\n\n"));
            }
            else //Paused / Resumed
            {
                String firstOutput, secondOutput = "", newButtonText;
                boolean updateThread;

                if(TaskControlController.this.taskThread.getPaused()) //Paused
                {
                    firstOutput = "\nWaiting for the task to pause...\n\n";
                    newButtonText = "Resume";
                    secondOutput = "\nTask paused!\n\n";
                    updateThread = true;
                }
                else //Resumed
                {
                    firstOutput = "\nTask resumed!\n\n";
                    newButtonText = "Pause";
                    updateThread = false;
                }

                TaskControlController.this.PauseButton.setDisable(true);
                TaskControlController.this.stopButton.setDisable(true);
                TaskControlController.this.logTextArea.appendText(firstOutput);

                if(TaskControlController.this.taskThread.getPaused())
                    while(!TaskControlController.this.taskThread.getExecutor().isTerminated()) {}

                TaskControlController.this.updateThreadButton.setVisible(updateThread);
                TaskControlController.this.updateThreadButton.setDisable(!updateThread);
                TaskControlController.this.numberOfThreadToExecuteLabel.setDisable(!updateThread);
                TaskControlController.this.threadsSpinner.setDisable(!updateThread);

                String finalSecondOutput = secondOutput;
                Platform.runLater(() ->
                        {
                            TaskControlController.this.logTextArea.appendText(finalSecondOutput);
                            TaskControlController.this.PauseButton.setText(newButtonText);
                        });
                TaskControlController.this.PauseButton.setDisable(false);
                TaskControlController.this.stopButton.setDisable(false);
            }

            TaskControlController.this.taskThread.resetStatusChanged();
        }
    }

    @FXML private ScrollPane scrollPane;
    @FXML private BorderPane taskBorderPane;
    @FXML private ToolBar toolBar;
    @FXML private ComboBox<String> TaskTargetSelection;
    @FXML private HBox toolBarHBox;
    @FXML private ProgressBar progressBar;
    @FXML private Label targetsFinishedLabel;
    @FXML private Label progressBarLabel;
    @FXML private Button addSelectedButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearTableButton;
    @FXML private Button runButton;
    @FXML private Button PauseButton;
    @FXML private Button stopButton;
    @FXML private Button selectAllButton;
    @FXML private Button deselectAllButton;
    @FXML private Pane leftPane;
    @FXML private ComboBox<String> taskSelection;
    @FXML private ComboBox<String> targetSelection;
    @FXML private ComboBox<String> affectedTargets;
    @FXML private ListView<String> currentSelectedTargetListView;
    @FXML private TabPane tabPane;
    @FXML private Tab tableViewTabPane;
    @FXML private TableView<TaskTargetInformation> taskTargetDetailsTableView;
    @FXML private TableColumn<TaskTargetInformation, Integer> numberColumn;
    @FXML private TableColumn<TaskTargetInformation, String> targetNameColumn;
    @FXML private TableColumn<TaskTargetInformation, String> positionColumn;
    @FXML private TableColumn<TaskTargetInformation, String> currentRuntimeStatusColumn;
    @FXML private TableColumn<TaskTargetInformation, String> resultStatusColumn;
    @FXML private Tab graphViewTabPane;
    @FXML private Tab graphViewTabPane1;
    @FXML private Pane footerPane;
    @FXML private AnchorPane AnchorPane;
    @FXML private RadioButton fromScratchRadioButton;
    @FXML private ToggleGroup scratchOrIncremental;
    @FXML private RadioButton incrementalRadioButton;
    @FXML private Label processingTimeLabel;
    @FXML private Label limitedPermanentLabel;
    @FXML private Label successRateLabel;
    @FXML private TextField processingTimeTextField;
    @FXML private TextArea logTextArea;
    @FXML private Label successRateWithWarnings;
    @FXML private Spinner<?> processingTimeSpinner;
    @FXML private RadioButton limitedRadioButton;
    @FXML private ToggleGroup limitedOrPermanent;
    @FXML private RadioButton permanentRadioButton;
    @FXML private Slider successRateSlider;
    @FXML private Slider successRateWithWarningsSlider;
    @FXML private Tab logPane;
    @FXML private TextField successWithWarningRateText;
    @FXML private TextField successRateText;
    @FXML private Button ApplyParametersButton;
    @FXML private TextArea taskDetailsOnTargetTextArea;
    @FXML private Spinner<Integer> threadsSpinner;
    @FXML private Label numberOfThreadToExecuteLabel;
    @FXML private Tab compilationTab;
    @FXML private Pane leftPaneCompilation;
    @FXML private Button toCompileButton;
    @FXML private Button compiledOutputButton;
    @FXML private TabPane SimulationCompilationTabPane;
    @FXML private Tab simulationTab;
    @FXML private Button updateThreadButton;
    @FXML private Label compilationSourceCodeLabel;
    @FXML private Label compilationOutputLabel;
    @FXML private Label sourceCodePathLabel;
    @FXML private Label outputPathLabel;

    @FXML
    void updateThreadsInPause(ActionEvent event) {
        this.numOfThreads = this.threadsSpinner.getValue();
        this.taskThread.setNumOfThreads(this.numOfThreads);
    }
    @FXML
    void chooseSourceCodeDirectoryToCompile(ActionEvent event)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        this.sourceCodeDirectory = directoryChooser.showDialog(this.taskBorderPane.getParent().getScene().getWindow());
        if(this.sourceCodeDirectory != null)
            this.sourceCodePathLabel.setText("Source Code Path : " + this.sourceCodeDirectory.getAbsolutePath());

        this.runButton.setDisable(this.sourceCodeDirectory == null || this.outputDirectory == null || this.taskTargetDetailsTableView.getItems().isEmpty());
    }
    @FXML
    void chooseOutputDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        this.outputDirectory = directoryChooser.showDialog(this.taskBorderPane.getParent().getScene().getWindow());
        if(this.outputDirectory != null)
            this.outputPathLabel.setText("Output Path : " + this.outputDirectory.getAbsolutePath());

        this.runButton.setDisable(this.sourceCodeDirectory == null || this.outputDirectory == null || this.taskTargetDetailsTableView.getItems().isEmpty());
    }

    @FXML void removeSelectedRowFromTable(ActionEvent event)
    {
        if(this.taskTargetDetailsTableView.getItems().size()>0)
        {
            TaskTargetInformation chosenTarget = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
            if(chosenTarget!=null) {
                int index = chosenTarget.getNumber() - 1, size = this.taskTargetDetailsTableView.getItems().size();

                this.taskTargetDetailsTableView.getItems().remove(chosenTarget);

                while (size - 1 > index) {
                    chosenTarget = this.taskTargetDetailsTableView.getItems().get(index);
                    chosenTarget.setNumber(++index);
                }

                updateTargetTaskDetailsInTextArea();
                turnOnIncrementalButton();

                if (size - 1 == 0) {
                    this.runButton.setDisable(true);
                    this.clearTableButton.setDisable(false);
                }
            }
        }
        if(this.taskTargetDetailsTableView.getItems().isEmpty())
        {
            this.removeSelectedButton.setDisable(true);
            this.clearTableButton.setDisable(true);
        }
    }

    private void updateTargetTaskDetailsInTextArea() {
        if(!this.taskTargetDetailsTableView.getItems().isEmpty())
        {
            TaskTargetInformation taskTargetInformation = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
            showDetailsOfSelectedTargetInTextArea(taskTargetInformation);
        }
        else
            enableTargetInfoTextArea(false);
    }

    @FXML void ClearTable(ActionEvent event)
    {
        this.taskTargetDetailsTableView.getItems().clear();
        enableTargetInfoTextArea(false);
        this.runButton.setDisable(true);
        this.clearTableButton.setDisable(true);
        this.removeSelectedButton.setDisable(true);
    }

    @FXML void addSelectedTargetsToTable(ActionEvent event)
    {
        setTaskTargetDetailsTable();
        this.incrementalRadioButton.setDisable(!incrementalIsOptional());

        if(this.taskType.equals(TaskThread.TaskType.Compilation))
        {
            if(this.outputDirectory != null && this.sourceCodeDirectory != null)
                this.runButton.setDisable(false);
        }
        else
        {
            if(this.taskParameters != null)
                this.runButton.setDisable(false);
        }
        this.clearTableButton.setDisable(false);
    }

    private void enableTargetInfoTextArea(boolean flag) {
        this.taskDetailsOnTargetTextArea.setVisible(flag);
        this.taskDetailsOnTargetTextArea.setDisable(!flag);
    }

    private void turnOnIncrementalButton() {
        boolean change = false;

        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
        {
            if(curr.getResultStatus().equals("Undefined"))
            {
                change = true;
                break;
            }
        }
        this.incrementalRadioButton.setDisable(change);
    }

    @FXML public void initialize()
    {
        initializeGraphDetails();
    }

    @FXML void affectedTargetsPressed(ActionEvent event) {
        Set<String> affectedTargetsSet = null;

        switch (this.affectedTargets.getValue())
        {
            case DEPENDED:
            {
                affectedTargetsSet = this.graph.getTarget(this.targetSelection.getValue()).getAllDependsOnTargets();
                break;
            }
            case REQUIRED:
            {
                affectedTargetsSet = this.graph.getTarget(this.targetSelection.getValue()).getAllRequiredForTargets();
                break;
            }
            default:
                break;
        }

        if(affectedTargetsSet != null)
        {
            this.currentSelectedTargets.clear();
            this.currentSelectedTargets.add(this.targetSelection.getValue());
            this.currentSelectedTargets.addAll(affectedTargetsSet);

            this.incrementalRadioButton.setDisable(incrementalIsOptional());
        }
    }

    private boolean incrementalIsOptional() {
        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
        {
            if(curr.getResultStatus().equals("Undefined"))
                return false;
        }
        return true;
    }

    @FXML private Label currentSelectedTargetLabel;

    @FXML void deselectAllPressed(ActionEvent event) {
        this.currentSelectedTargets.clear();
    }

    @FXML void graphViewTabPressed(Event event) {}

    @FXML void limitedOptionPressed(ActionEvent event) {}

    @FXML void logViewTabPressed(Event event) {}

    @FXML void pausePressed(ActionEvent event) {
        if(!this.taskThread.getPaused()) //Pausing the task
        {
            this.PauseButton.setDisable(true);
            this.stopButton.setDisable(true);
            this.taskThread.pauseTheTask();
        }
        else //Resuming the task
            this.taskThread.continueTheTask();
    }

    @FXML void permanentOptionPressed(ActionEvent event) {}

    @FXML void runPressed(ActionEvent event) {
        if(!checkForValidRun())
            return;

        CompilationParameters compilationParameters = null;
        Thread updateThread = new Thread(this::updateTableRuntimeStatuses);
        TaskThreadWatcher taskThreadWatcher = new TaskThreadWatcher();
        Set<String> currentRunTargets = setCurrentRunTargets();
        TaskOutput taskOutput = new TaskOutput(this.logTextArea, this.graphSummary, this.graph);
        turnOnProgressBar();

        if(this.taskType.equals(TaskThread.TaskType.Simulation))
            applyTaskParametersForAllTargets(this.taskParameters);
        else //Compilation
        {
            compilationParameters = new CompilationParameters(this.sourceCodeDirectory, this.outputDirectory);
            this.numOfThreads = this.threadsSpinner.getValue();
        }

        this.taskDetailsOnTargetTextArea.setDisable(false);
        this.progressBar.setDisable(false);
        this.progressBarLabel.setDisable(false);
        this.targetsFinishedLabel.setDisable(false);

        this.taskThread = new TaskThread(this.graph, this.taskType, this.taskParametersMap, compilationParameters, this.graphSummary,
                currentRunTargets, this.numOfThreads, taskOutput, this.incrementalRadioButton.isSelected());

        taskThreadWatcher.setDaemon(true);

        this.taskThread.start();
        createNewProgressBar();
        taskThreadWatcher.start();
        updateThread.start();
    }

    private void turnOnProgressBar() {
        this.progressBar.setDisable(false);
        this.progressBarLabel.setDisable(false);
        this.targetsFinishedLabel.setDisable(false);
    }

    private Set<String> setCurrentRunTargets() {
        Set<String> currentRunTargets = new HashSet<>();

        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
            currentRunTargets.add(curr.getTargetName());

        return currentRunTargets;
    }

    private void createNewProgressBar()
    {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxSize = TaskControlController.this.taskTargetDetailsTableView.getItems().size();
                while (TaskControlController.this.taskThread.isAlive()) {
                    Thread.sleep(200);
                    getFinishedTargetsInRealTime();
                    updateProgress(TaskControlController.this.finishedTargets, maxSize);
                }
                updateProgress(maxSize, maxSize);
                return null;
            }
        };
        this.progressBar.setStyle("-fx-accent: #00FF00;");
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressBarLabel.textProperty().bind
                (Bindings.concat(Bindings.format("%.0f", Bindings.multiply(task.progressProperty(), 100)), " %"));

        Thread progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);
        progressBarThread.start();
    }

    private Boolean checkForValidRun()
    {
        String errorMessage = "";
        boolean validIncremental = false;

        if(this.taskType.equals(TaskThread.TaskType.Simulation))
        {
            if(this.taskParameters == null)
                errorMessage = "You have to apply the parameters for the task first!";
        }
        else //Compilation task
        {
            if(this.sourceCodeDirectory == null || this.outputDirectory == null)
                errorMessage = "Please choose directories for compilation task!";
        }

        if(this.incrementalRadioButton.isSelected())
        {
            if(this.incrementalRadioButton.isDisabled())
                errorMessage = "Incremental is not optional!\nChoose \"From Scratch\" or select other targets";
            else
            {
                for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
                {
                    if(curr.getResultStatus().equals("Undefined") || curr.getResultStatus().equals("Failure"))
                        validIncremental = true; break;
                }

                if(!validIncremental)
                    errorMessage = "There are no targets available for the current task!";
            }
        }

        if(!errorMessage.equals(""))
        {
            ShowPopup(errorMessage, "Can't start task");
            return false;
        }
        return true;
    }

    @FXML void selectAllPressed(ActionEvent event) {
        this.currentSelectedTargets.clear();
        this.graph.getGraphTargets().values().forEach(targetName -> this.currentSelectedTargets.add(targetName.getTargetName()));
    }

    @FXML void stopPressed(ActionEvent event) {
        this.taskThread.stopTheTask();
    }

    @FXML void tableViewTabPressed(Event event) {}

    @FXML void targetSelectionPressed(ActionEvent event) {
        this.affectedTargets.setDisable(false);

        this.currentSelectedTargets.clear();
        this.currentSelectedTargets.add(this.targetSelection.getValue());

        if(this.affectedTargets.getValue() != null)
            affectedTargetsPressed(event);
    }

    @FXML void taskSelectionPressed(ActionEvent event) {
        if(!this.taskSelection.getSelectionModel().isEmpty())
        {
            setForSimulationTask(!this.taskSelection.getValue().equals(this.SIMULATION));
            disableButtons(false);
            this.threadsSpinner.setVisible(true);
            this.numberOfThreadToExecuteLabel.setVisible(true);
            this.threadsSpinner.setDisable(false);
            this.fromScratchRadioButton.setDisable(false);
            this.numberOfThreadToExecuteLabel.setDisable(false);
        }

        this.taskType = this.taskSelection.getValue().equals("Simulation") ? TaskThread.TaskType.Simulation : TaskThread.TaskType.Compilation;
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
                this.successRateText.setText(String.valueOf(1.0));
                this.successRateSlider.setValue(1.0);
            }
            else if(Double.parseDouble(newValue) < 0.0)
            {
                this.successRateText.setText(String.valueOf(0.0));
                this.successRateSlider.setValue(0.0);
            }
            else
                this.successRateSlider.setValue(Double.parseDouble(newValue));
        } catch(Exception ex)
        {
            ShowPopup("Invalid input in task parameters!", "Invalid input");
            this.successRateText.clear();
        }
        });

        this.successWithWarningRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
            if(newValue.equals(""))
                return;

            if(Double.parseDouble(newValue) > 1.0)
            {
                this.successRateWithWarningsSlider.setValue(1.0);
                this.successWithWarningRateText.setText(String.valueOf(1.0));
            }
            else if(Double.parseDouble(newValue) < 0.0)
            {
                this.successRateWithWarningsSlider.setValue(0.0);
                this.successWithWarningRateText.setText(String.valueOf(0.0));
            }
            else
                this.successRateWithWarningsSlider.setValue(Double.parseDouble(newValue));
            } catch(Exception ex)
            {
                ShowPopup("Invalid input in task parameters!", "Invalid input");
                this.successWithWarningRateText.clear();
            }
        });
    }

    private void addListenersForSliders() {
        this.successRateSlider.valueProperty().addListener((observable, oldValue, newValue) -> this.successRateText.setText(String.format("%.3f", newValue)));
        this.successRateWithWarningsSlider.valueProperty().addListener((observable, oldValue, newValue) -> this.successWithWarningRateText.setText(String.format("%.3f", newValue)));
    }

    private void disableButtons(Boolean flag) {

        this.targetSelection.setDisable(flag);
        this.affectedTargets.setDisable(flag);
        this.currentSelectedTargetLabel.setDisable(flag);
        this.currentSelectedTargetListView.setDisable(flag);

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
        this.addSelectedButton.setDisable(flag);

        this.threadsSpinner.setDisable(flag);
        this.numberOfThreadToExecuteLabel.setDisable(flag);

        setForSimulationTask(flag);
    }

    public void setGraph(Graph graph, GraphSummary graphSummary) {
        this.graph = graph;
        this.graphSummary = graphSummary;
        setAllTargetsList();
        setTaskTargetDetailsTable();
    }

    private void applyTaskParametersForAllTargets(SimulationParameters taskParameters) {
        this.taskParametersMap = new HashMap<>();
        SimulationParameters currTaskParameters;
        Duration processingTime;
        long randomTime;
        double successRate = taskParameters.getSuccessRate(), successRateWithWarnings = taskParameters.getSuccessWithWarnings();
        Boolean isRandom = taskParameters.isRandom();

        //permanent time for all targets
        if(!isRandom)
        {
            for(Target target : this.graph.getGraphTargets().values())
                this.taskParametersMap.put(target.getTargetName(), taskParameters);

            return;
        }

        //Random time for each target
        for(Target target : this.graph.getGraphTargets().values())
        {
            processingTime = taskParameters.getProcessingTime();
            randomTime = (long)(Math.random() * (processingTime.toMillis())) + 1;
            processingTime = (Duration.of(randomTime, ChronoUnit.MILLIS));

            currTaskParameters = new SimulationParameters(processingTime, isRandom, successRate, successRateWithWarnings);
            this.taskParametersMap.put(target.getTargetName(), currTaskParameters);
        }
    }

    private void setAllTargetsList() {
        int i = 0;
        this.allTargetsList = FXCollections.observableArrayList();

        for(Target currentTargetName : this.graph.getGraphTargets().values())
            this.allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = this.allTargetsList.sorted();
        this.targetSelection.getItems().addAll(sorted);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> taskSelectionList = FXCollections.observableArrayList(this.SIMULATION, this.COMPILATION);
        this.taskSelection.setItems(taskSelectionList);

        addListenersForSliders();
        addListenersForTextFields();
        addListenersForSelectedTargets();
        addListenersToButtons();
        addListenersForCompilationButtons();

        String NONE = "none";
        this.affectedTargetsOptions.addAll(NONE, this.DEPENDED, this.REQUIRED);
        this.affectedTargets.setItems(this.affectedTargetsOptions);

        initializeGraphDetails();
    }

    private void addListenersForCompilationButtons() {

        this.taskSelection.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    setVisibilityOfTask(TaskControlController.this.taskSelection.getValue().equals(TaskControlController.this.COMPILATION));
            }
        });
    }

    private void setVisibilityOfTask(boolean flag)
    {
        //Compilation
        this.compilationOutputLabel.setVisible(flag);
        this.compilationSourceCodeLabel.setVisible(flag);
        this.toCompileButton.setVisible(flag);
        this.compiledOutputButton.setVisible(flag);
        this.sourceCodePathLabel.setVisible(flag);
        this.outputPathLabel.setVisible(flag);

        //Simulation
        this.processingTimeLabel.setVisible(!flag);
        this.processingTimeTextField.setVisible(!flag);
        this.successRateSlider.setVisible(!flag);
        this.successRateWithWarningsSlider.setVisible(!flag);
        this.limitedPermanentLabel.setVisible(!flag);
        this.limitedRadioButton.setVisible(!flag);
        this.permanentRadioButton.setVisible(!flag);
        this.successWithWarningRateText.setVisible(!flag);
        this.successRateText.setVisible(!flag);
        this.ApplyParametersButton.setVisible(!flag);
        this.successRateLabel.setVisible(!flag);
        this.successRateWithWarnings.setVisible(!flag);
    }

    private void setSpinnerNumericBounds()
    {
        this.threadsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, this.maxParallelThreads));
    }

    private void addListenersForSelectedTargets() {
        //Enable/Disable incremental, selectAll, deselectAll button
        this.currentSelectedTargets.addListener((ListChangeListener<String>) c -> {
            boolean containAll = TaskControlController.this.currentSelectedTargets.containsAll(TaskControlController.this.allTargetsList);
            TaskControlController.this.selectAllButton.setDisable(containAll);
            TaskControlController.this.deselectAllButton.setDisable(!containAll);

            while (c.next()) {
                for (String remitem : c.getRemoved()) {
                    TaskControlController.this.currentSelectedTargetListView.getItems().remove(remitem);
                    TaskControlController.this.addSelectedButton.setDisable(true);
                }
                for (String additem : c.getAddedSubList()) {
                    TaskControlController.this.currentSelectedTargetListView.getItems().add(additem);
                    TaskControlController.this.addSelectedButton.setDisable(false);
                }
            }

            if(TaskControlController.this.currentSelectedTargets.isEmpty())
                TaskControlController.this.addSelectedButton.setDisable(true);
        });
    }

    private void addListenersToButtons()
    {
        this.taskTargetDetailsTableView.getItems().addListener(new ListChangeListener<TaskTargetInformation>() {
            @Override
            public void onChanged(Change<? extends TaskTargetInformation> c) {
                TaskControlController.this.removeSelectedButton.setDisable(c.getList().isEmpty());
               // TaskController.this.clearTableButton.setDisable(c.getList().isEmpty());

                TaskControlController.this.incrementalRadioButton.setDisable(!incrementalIsOptional());
            }
        });
    }

    public SimulationParameters getSimulationTaskParametersFromUser() {
        SimulationParameters taskParameters = new SimulationParameters();
        Duration processingTime;
        long timeInMS;
        boolean isRandom;
        double successRate, successWithWarnings;

        try {
            timeInMS = Integer.parseInt(this.processingTimeTextField.getText());
            processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);

            isRandom = this.limitedRadioButton.isSelected();
            successRate = this.successRateSlider.getValue();
            successWithWarnings = this.successRateWithWarningsSlider.getValue();

            taskParameters.setProcessingTime(processingTime);
            taskParameters.setRandom(isRandom);
            taskParameters.setSuccessRate(successRate);
            taskParameters.setSuccessWithWarnings(successWithWarnings);
            this.numOfThreads = this.threadsSpinner.getValue();
        }
        catch(Exception ex)
        {
            ShowPopup("Invalid input in parameters.", "Invalid Parameters");
        }

        return taskParameters;
    }

    public void setMaxParallelThreads(int parallelThreads) {
        this.maxParallelThreads = parallelThreads;
        setSpinnerNumericBounds();
    }

    @FXML void ApplyParametersToTask(ActionEvent event) {
        this.taskParameters = getSimulationTaskParametersFromUser();

        if(this.taskParameters.getProcessingTime()!=null) // Checking only on the processing time, because other parameters are already initialized
            this.runButton.setDisable(false);
    }

    private void ShowPopup(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML void fromScratchOptionPressed(ActionEvent event) { }

    @FXML void incrementalOptionPressed(ActionEvent event) { }

    //-----------------------------------------------------------------------------------------------
    private void initializeGraphDetails() {
        this.numberColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, Integer>("number"));
        this.targetNameColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("targetName"));
        this.positionColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("position"));
        this.currentRuntimeStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("currentRuntimeStatus"));
        this.resultStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetInformation, String>("resultStatus"));

        this.taskTargetDetailsTableView.setRowFactory(tv -> new TableRow<TaskTargetInformation>()
        {
            protected void updateItem(TaskTargetInformation item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null)
                    setStyle("");
                else if (item.getCurrentRuntimeStatus().equals("Skipped"))
                    setStyle("-fx-background-color: gray;");
                else if (item.getResultStatus().equals("Failure"))
                    setStyle("-fx-background-color: #f33c3c;" + "-fx-text-fill: white;");
                else if (item.getResultStatus().equals("Success"))
                    setStyle("-fx-background-color: #1bff1b;" + "-fx-text-fill: white;");
                else if (item.getResultStatus().equals("Warning"))
                    setStyle("-fx-background-color: orange;");
                else if (item.getCurrentRuntimeStatus().equals("In process"))
                    setStyle("-fx-background-color: yellow;");
                else if (item.getCurrentRuntimeStatus().equals("Frozen"))
                    setStyle("-fx-background-color: #469eff;");
                else if (item.getCurrentRuntimeStatus().equals("Waiting"))
                    setStyle("-fx-background-color: #e47bff;");
            }
        });
    }

    private void setTaskTargetDetailsTable()
    {
        int i = this.taskTargetDetailsTableView.getItems().size() + 1;
        String targetPosition, targetRuntimeStatus, targetResultStatus;
        TaskTargetInformation taskTargetInformation;
        ObservableList<TaskTargetInformation> tableList = this.taskTargetDetailsTableView.getItems();

        for(String currentTarget : this.currentSelectedTargetListView.getItems())
        {
            if(!targetExistedInTable(tableList, currentTarget)) {
                targetPosition = this.graph.getTarget(currentTarget).getTargetPosition().toString();
                targetRuntimeStatus = this.graphSummary.getTargetsSummaryMap().get(currentTarget).getRuntimeStatus().toString();
                targetResultStatus = this.graphSummary.getTargetsSummaryMap().get(currentTarget).getResultStatus().toString();
                taskTargetInformation = new TaskTargetInformation(i++, currentTarget, targetPosition, targetRuntimeStatus, targetResultStatus);
                this.taskTargetDetailsList.add(taskTargetInformation);
            }
        }
        this.taskTargetDetailsTableView.setItems(this.taskTargetDetailsList);
    }

    private void updateTableRuntimeStatuses()
    {
        ObservableList<TaskTargetInformation> itemsList = this.taskTargetDetailsTableView.getItems();
        LocalTime startTime = LocalTime.now();
        LocalTime currTime = LocalTime.now();

        while (this.taskThread.isAlive())
        {
            startTime = LocalTime.now();
            updateTable(itemsList, startTime, currTime);
        }
        updateTable(itemsList, startTime, currTime);
        TaskControlController.this.incrementalRadioButton.setDisable(!incrementalIsOptional());
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
            item.setCurrentRuntimeStatus(this.graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getRuntimeStatus().toString());
            item.setResultStatus(this.graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getResultStatus().toString());
        }
        Platform.runLater(()->{this.taskTargetDetailsTableView.refresh();});
    }

    public void getSelectedRow(MouseEvent mouseEvent)
    {
        updateTargetTaskDetailsInTextArea();
        enableTargetInfoTextArea(true);
        this.removeSelectedButton.setDisable(false);
    }

    private boolean targetExistedInTable(ObservableList<TaskTargetInformation> tableList, String currentTargetName) {
        for(TaskTargetInformation currInfo : tableList)
        {
            if(currInfo.getTargetName().equals(currentTargetName))
                return true;
        }
        return false;
    }

    public void showDetailsOfSelectedTargetInTextArea(TaskTargetInformation taskTargetInformation)
    {
        String detailMsg = null;
        String currentTargetName = taskTargetInformation.getTargetName();
        TargetSummary currentTargetSummary = this.graphSummary.getTargetsSummaryMap().get(currentTargetName);
        if(currentTargetName!=null) {
            Target currentTarget = this.graph.getTarget(currentTargetName);
             detailMsg = "Target : " + currentTargetName + "\n"
                    + "Position : " + currentTarget.getTargetPosition() + "\n";

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
                    detailMsg += "The target " + currentTargetName + " is waiting for : " +
                            (currentTargetSummary.currentWaitingTime().toMillis() - currentTargetSummary.getTotalPausingTime().toMillis()) + " m/s";
                    break;
                }
                case InProcess:
                {
                    detailMsg += "The target " + currentTargetName + " is in process for : " + currentTargetSummary.currentProcessingTime().toMillis() + " m/s";
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
                        detailMsg += "Target's running time: " + time.toMillis() + "m/s\n";
                    break;
                }
            }
        }

        this.taskDetailsOnTargetTextArea.setText(detailMsg);
    }

    public String printTargetWaitingForTargets(String currentTargetName)
    {
        String waitingForTargets = "", dependedOnTarget;
        Set<String> dependedTargets = this.graph.getTarget(currentTargetName).getAllDependsOnTargets();

        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
        {
            dependedOnTarget = curr.getTargetName();
            if(dependedTargets.contains(dependedOnTarget))
            {
                if(!this.graphSummary.getTargetsSummaryMap().get(dependedOnTarget).getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished))
                    waitingForTargets = waitingForTargets + dependedOnTarget + " ";
            }
        }
        return waitingForTargets;
    }

    public String printProcessedFailedTargets(String currentTargetName)
    {
        String processedFailedTargets = "", dependedOnTarget;
        Set<String> dependedTargets = this.graph.getTarget(currentTargetName).getAllDependsOnTargets();

        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
        {
            dependedOnTarget = curr.getTargetName();
            if (dependedTargets.contains(dependedOnTarget))
            {
                if (this.graphSummary.getTargetsSummaryMap().get(dependedOnTarget).getResultStatus().equals(TargetSummary.ResultStatus.Failure))
                    processedFailedTargets = processedFailedTargets + dependedOnTarget + " ";
            }
        }
        return processedFailedTargets;
    }

    public void getFinishedTargetsInRealTime()
    {
        this.finishedTargets = 0;
        for(TaskTargetInformation currItem : this.taskTargetDetailsTableView.getItems())
        {
            if(currItem.getCurrentRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished.toString())||currItem.getCurrentRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped.toString()))
                this.finishedTargets++;
        }
    }
}
