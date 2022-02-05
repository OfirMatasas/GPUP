package controllers;

import information.TaskTargetInformation;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;
import task.TaskThread;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

public class TaskControlController {

    private final String REQUIRED = "All required-for targets";
    private final String DEPENDED = "All depends-on targets";
    private final ObservableList<TaskTargetInformation> taskTargetDetailsList = FXCollections.observableArrayList();
    private TaskThread taskThread;
    private GraphSummary graphSummary;
    private int finishedTargets;

    @FXML private ScrollPane scrollPane;
    @FXML private BorderPane taskBorderPane;
    @FXML private ToolBar toolBar;
    @FXML private HBox toolBarHBox;
    @FXML private Button runButton;
    @FXML private Button PauseButton;
    @FXML private Button stopButton;
    @FXML private Pane leftPane;
    @FXML private RadioButton fromScratchRadioButton;
    @FXML private ToggleGroup scratchOrIncremental;
    @FXML private RadioButton incrementalRadioButton;
    @FXML private TextField TaskNameTextField;
    @FXML private TextField GraphNameTextField;
    @FXML private TextField NumberOfWorkersTextField;
    @FXML private TableView<TaskTargetInformation> taskTargetDetailsTableView;
    @FXML private TableColumn<TaskTargetInformation, Integer> numberColumn;
    @FXML private TableColumn<TaskTargetInformation, String> targetNameColumn;
    @FXML private TableColumn<TaskTargetInformation, String> positionColumn;
    @FXML private TableColumn<TaskTargetInformation, String> currentRuntimeStatusColumn;
    @FXML private TableColumn<TaskTargetInformation, String> resultStatusColumn;
    @FXML private TextArea taskDetailsOnTargetTextArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label targetsFinishedLabel;
    @FXML private Label progressBarLabel;
    @FXML private Pane zeroSuccessRate;
    @FXML private TextArea logTextArea;
    private Graph graph;
    private TaskControlPullerThread taskControlPullerThread;

    //----------------------------------------------Puller Thread--------------------------------------------//
    public class TaskControlPullerThread extends Thread
    {
        @Override
        public void run()
        {
//            disableTaskOptions(true);
//            TaskControlController.this.PauseButton.setDisable(false);
//            TaskControlController.this.stopButton.setDisable(false);
//
//            while(TaskControlController.this.taskThread.isAlive())
//            {
//                if(TaskControlController.this.taskThread.getStatusChanged())
//                    taskPausedOrStopped();
//            }
//
//            disableTaskOptions(false);
//            TaskControlController.this.PauseButton.setDisable(true);
//            TaskControlController.this.stopButton.setDisable(true);
//
//            Platform.runLater(() -> TaskControlController.this.PauseButton.setText("Pause"));
//        }
//
//        public void taskPausedOrStopped()
//        {
//            if(TaskControlController.this.taskThread.getStopped()) //Stopped
//            {
//                if(!TaskControlController.this.taskThread.getPaused())
//                    TaskControlController.this.logTextArea.appendText("\nWaiting for the task to stop...\n\n");
//
//                while(!TaskControlController.this.taskThread.getExecutor().isTerminated()) {}
//
//                Platform.runLater(() -> TaskControlController.this.logTextArea.appendText("\nTask stopped!\n\n"));
//            }
//            else //Paused / Resumed
//            {
//                String firstOutput, secondOutput = "", newButtonText;
//                boolean updateThread;
//
//                if(TaskControlController.this.taskThread.getPaused()) //Paused
//                {
//                    firstOutput = "\nWaiting for the task to pause...\n\n";
//                    newButtonText = "Resume";
//                    secondOutput = "\nTask paused!\n\n";
//                    updateThread = true;
//                }
//                else //Resumed
//                {
//                    firstOutput = "\nTask resumed!\n\n";
//                    newButtonText = "Pause";
//                    updateThread = false;
//                }
//
//                TaskControlController.this.PauseButton.setDisable(true);
//                TaskControlController.this.stopButton.setDisable(true);
//                TaskControlController.this.logTextArea.appendText(firstOutput);
//
//                if(TaskControlController.this.taskThread.getPaused())
//                    while(!TaskControlController.this.taskThread.getExecutor().isTerminated()) {}
//
//                String finalSecondOutput = secondOutput;
//                Platform.runLater(() ->
//                {
//                    TaskControlController.this.logTextArea.appendText(finalSecondOutput);
//                    TaskControlController.this.PauseButton.setText(newButtonText);
//                });
//                TaskControlController.this.PauseButton.setDisable(false);
//                TaskControlController.this.stopButton.setDisable(false);
//            }
//
//            TaskControlController.this.taskThread.resetStatusChanged();
        }
    }

    //-------------------------------------------------Initialize-----------------------------------------------//
    @FXML public void initialize()
    {
        this.taskControlPullerThread = new TaskControlPullerThread();
        initializeGraphDetails();
    }

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

    //--------------------------------------------Target information----------------------------------------------//
    @FXML void getSelectedRow(MouseEvent event) {
        updateTargetTaskDetailsInTextArea();
        enableTargetInfoTextArea(true);
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

    private void enableTargetInfoTextArea(boolean flag) {
        this.taskDetailsOnTargetTextArea.setVisible(flag);
        this.taskDetailsOnTargetTextArea.setDisable(!flag);
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

    //------------------------------------------Preparations For Launch-------------------------------------------//
    private boolean incrementalIsOptional() {
        for(TaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
        {
            if(curr.getResultStatus().equals("Undefined"))
                return false;
        }
        return true;
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

    //------------------------------------------------Progress Bar------------------------------------------------//
    private void turnOnProgressBar() {
        this.progressBar.setDisable(false);
        this.progressBarLabel.setDisable(false);
        this.targetsFinishedLabel.setDisable(false);
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

    //-------------------------------------------------During Task------------------------------------------------//
    @FXML
    void runPressed(ActionEvent event) {
//        if(!checkForValidRun())
//            return;
//
//        CompilationParameters compilationParameters = null;
//        Thread updateThread = new Thread(this::updateTableRuntimeStatuses);
//        TaskThreadWatcher taskThreadWatcher = new TaskThreadWatcher();
//        Set<String> currentRunTargets = setCurrentRunTargets();
//        TaskOutput taskOutput = new TaskOutput(this.logTextArea, this.graphSummary, this.graph);
//        turnOnProgressBar();
//
//        if(this.taskType.equals(TaskThread.TaskType.Simulation))
//            applyTaskParametersForAllTargets(this.taskParameters);
//        else //Compilation
//        {
//            compilationParameters = new CompilationParameters(this.sourceCodeDirectory, this.outputDirectory);
//            this.numOfThreads = this.threadsSpinner.getValue();
//        }
//
//        this.taskDetailsOnTargetTextArea.setDisable(false);
//        this.progressBar.setDisable(false);
//        this.progressBarLabel.setDisable(false);
//        this.targetsFinishedLabel.setDisable(false);
//
//        this.taskThread = new TaskThread(this.graph, this.taskType, this.taskParametersMap, compilationParameters, this.graphSummary,
//                currentRunTargets, this.numOfThreads, taskOutput, this.incrementalRadioButton.isSelected());
//
//        taskThreadWatcher.setDaemon(true);
//
//        this.taskThread.start();
//        createNewProgressBar();
//        taskThreadWatcher.start();
//        updateThread.start();
    }

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

    @FXML void stopPressed(ActionEvent event) {
        this.taskThread.stopTheTask();
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

    //----------------------------------------------------Other---------------------------------------------------//
    private void ShowPopup(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}