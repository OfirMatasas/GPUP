package controllers;

import com.google.gson.Gson;
import http.HttpClientUtil;
import information.AllTaskDetails;
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
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import summaries.GraphSummary;
import summaries.TargetSummary;
import tableItems.TaskTargetCurrentInfoTableItem;
import target.Graph;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Set;

public class AdminTaskControlController {

    private final ObservableList<TaskTargetCurrentInfoTableItem> taskTargetStatusesList = FXCollections.observableArrayList();
    private int finishedTargets;
    private String taskName = null;
    private String userName;
    private boolean isTaskRunning = false;
    private TaskControlPullerThread taskControlPullerThread;
    
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
    @FXML private TableView<TaskTargetCurrentInfoTableItem> taskTargetDetailsTableView;
    @FXML private TableColumn<TaskTargetCurrentInfoTableItem, Integer> numberColumn;
    @FXML private TableColumn<TaskTargetCurrentInfoTableItem, String> targetNameColumn;
    @FXML private TableColumn<TaskTargetCurrentInfoTableItem, String> currentRuntimeStatusColumn;
    @FXML private TableColumn<TaskTargetCurrentInfoTableItem, String> resultStatusColumn;
    @FXML private TextArea taskDetailsOnTargetTextArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label targetsFinishedLabel;
    @FXML private Label progressBarLabel;
    @FXML private Pane zeroSuccessRate;
    @FXML private TextArea logTextArea;
    private Graph graph;
    private GraphSummary graphSummary;

    //----------------------------------------------Puller Thread--------------------------------------------//
    public class TaskControlPullerThread extends Thread {
        @Override
        public void run()
        {
            while(true)
            {
                sendingThreadToSleep();

                if(AdminTaskControlController.this.isTaskRunning)
                    getTargetCurrentInfo();
                else
                    checkForIncremental();
            }
        }

        private void sendingThreadToSleep() {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void getTargetCurrentInfo() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("task-update", AdminTaskControlController.this.taskName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for task-update!"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                            {
                                AllTaskDetails updatedInfo = new Gson().fromJson(body, AllTaskDetails.class);
                                refreshInfo(updatedInfo);
                            }
                        );
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull task update from server!"));

                    Objects.requireNonNull(response.body()).close();
                }

                private void refreshInfo(AllTaskDetails updatedInfo) {
                    updateTargetStatusesTable(updatedInfo);
                    updateNumberOfWorkers(updatedInfo);
                    updateTaskLogHistory(updatedInfo);
                    checkIfTaskIsOver(updatedInfo);
                }

                private void updateTargetStatusesTable(AllTaskDetails updatedInfo) {
                    int selected = AdminTaskControlController.this.taskTargetDetailsTableView.getSelectionModel().getSelectedIndex();
                    AdminTaskControlController.this.taskTargetStatusesList.clear();
                    AdminTaskControlController.this.taskTargetStatusesList.addAll(updatedInfo.getTargetStatusSet());

                    AdminTaskControlController.this.taskTargetDetailsTableView.setItems(AdminTaskControlController.this.taskTargetStatusesList);
                    AdminTaskControlController.this.taskTargetDetailsTableView.getSelectionModel().select(selected);
                }

                private void updateNumberOfWorkers(AllTaskDetails updatedInfo) {
                    AdminTaskControlController.this.NumberOfWorkersTextField.setText(updatedInfo.getRegisteredWorkersNumber().toString());
                }

                private void updateTaskLogHistory(AllTaskDetails updatedInfo) {
                    if(updatedInfo.getTaskLogHistory() != null)
                    {
                        AdminTaskControlController.this.logTextArea.clear();
                        AdminTaskControlController.this.logTextArea.appendText(updatedInfo.getTaskLogHistory());
                    }
                }

                private void checkIfTaskIsOver(AllTaskDetails updatedInfo) {
                    if(updatedInfo.getTaskStatus().equalsIgnoreCase("Finished"))
                    {
                        String message = "The task " + updatedInfo.getTaskName() + " is over!";
                        Platform.runLater(() ->
                        {
                            AdminTaskControlController.this.isTaskRunning = false;
                            AdminTaskControlController.this.runButton.setDisable(false);
                            disablePauseAndStopButtons(true);
                            ShowPopup(Alert.AlertType.INFORMATION, "Task Finished!", null, message);
                        });
                    }
                }
            });
        }

        private void checkForIncremental() {
            if(incrementalIsOptional()) //Optional
                disableFromScratchAndIncrementalButtons(false);
            else //Not optional
            {
                disableFromScratchAndIncrementalButtons(true);
                AdminTaskControlController.this.fromScratchRadioButton.setSelected(true);
            }
        }
    }

    private void disableFromScratchAndIncrementalButtons(boolean flag) {
        this.incrementalRadioButton.setDisable(flag);
        this.fromScratchRadioButton.setDisable(flag);
    }

    //-------------------------------------------------Initialize-----------------------------------------------//
    public void initialize(String taskName, String userName) {
        this.taskName = taskName;
        this.userName = userName;
        createTaskControlPullerThread();
        initializeTaskDetailsTableView();
        this.taskControlPullerThread.getTargetCurrentInfo();
    }

    private void createTaskControlPullerThread() {
        this.taskControlPullerThread = new TaskControlPullerThread();
        this.taskControlPullerThread.setDaemon(true);
        this.taskControlPullerThread.start();
    }

    private void initializeTaskDetailsTableView() {
        this.numberColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetCurrentInfoTableItem, Integer>("targetNumber"));
        this.targetNameColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetCurrentInfoTableItem, String>("targetName"));
        this.currentRuntimeStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetCurrentInfoTableItem, String>("runtimeStatus"));
        this.resultStatusColumn.setCellValueFactory(new PropertyValueFactory<TaskTargetCurrentInfoTableItem, String>("resultStatus"));

        this.taskTargetDetailsTableView.setRowFactory(tv -> new TableRow<TaskTargetCurrentInfoTableItem>()
        {
            protected void updateItem(TaskTargetCurrentInfoTableItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null)
                    setStyle("");
                else if (item.getRuntimeStatus().equals("Skipped"))
                    setStyle("-fx-background-color: gray;");
                else if (item.getResultStatus().equals("Failure"))
                    setStyle("-fx-background-color: #f33c3c;" + "-fx-text-fill: white;");
                else if (item.getResultStatus().equals("Success"))
                    setStyle("-fx-background-color: #1bff1b;" + "-fx-text-fill: white;");
                else if (item.getResultStatus().equals("Warning"))
                    setStyle("-fx-background-color: orange;");
                else if (item.getRuntimeStatus().equals("In process"))
                    setStyle("-fx-background-color: yellow;");
                else if (item.getRuntimeStatus().equals("Frozen"))
                    setStyle("-fx-background-color: #469eff;");
                else if (item.getRuntimeStatus().equals("Waiting"))
                    setStyle("-fx-background-color: #e47bff;");
            }
        });
    }

    //---------------------------------------------Task information-----------------------------------------------//
    public void setTaskStaticInformation(String taskName, String graphName) {
        this.TaskNameTextField.setText(taskName);
        this.GraphNameTextField.setText(graphName);
    }

    //--------------------------------------------Target information----------------------------------------------//
    @FXML void getSelectedRow(MouseEvent event) {
        updateTargetTaskDetailsInTextArea();
        enableTargetInfoTextArea(true);
    }

    private void updateTargetTaskDetailsInTextArea() {
        if(!this.taskTargetDetailsTableView.getItems().isEmpty())
        {
            TaskTargetCurrentInfoTableItem taskTargetInformation = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
            showDetailsOfSelectedTargetInTextArea(taskTargetInformation);
        }
        else
            enableTargetInfoTextArea(false);
    }

    private void enableTargetInfoTextArea(boolean flag) {
        this.taskDetailsOnTargetTextArea.setVisible(flag);
        this.taskDetailsOnTargetTextArea.setDisable(!flag);
    }

    public void showDetailsOfSelectedTargetInTextArea(TaskTargetCurrentInfoTableItem taskTargetInformation) {
//        String detailMsg = null;
//        String currentTargetName = taskTargetInformation.getTargetName();
//        TargetSummary currentTargetSummary = this.graphSummary.getTargetsSummaryMap().get(currentTargetName);
//        if(currentTargetName != null) {
//            Target currentTarget = this.graph.getTarget(currentTargetName);
//            detailMsg = "Target : " + currentTargetName + "\n"
//                    + "Position : " + currentTarget.getTargetPosition() + "\n";
//
//            switch (currentTargetSummary.getRuntimeStatus())
//            {
//                case Frozen:
//                {
//                    detailMsg += "List of dependencies that the target " + currentTargetName + " is waiting for to finish : ";
//                    if(printTargetWaitingForTargets(currentTargetName).isEmpty())
//                        detailMsg += "none.";
//                    else
//                        detailMsg += printTargetWaitingForTargets(currentTargetName);
//                    break;
//                }
//                case Skipped:
//                {
//                    detailMsg += "Target's runtime status : Skipped \n";
//                    detailMsg += "List of dependencies that their process failed are : ";
//                    if(printProcessedFailedTargets(currentTargetName).isEmpty())
//                        detailMsg += "none.";
//                    else
//                        detailMsg += printProcessedFailedTargets(currentTargetName);
//                    break;
//                }
//                case Waiting:
//                {
//                    detailMsg += "The target " + currentTargetName + " is waiting for : " +
//                            (currentTargetSummary.currentWaitingTime().toMillis() - currentTargetSummary.getTotalPausingTime().toMillis()) + " m/s";
//                    break;
//                }
//                case InProcess:
//                {
//                    detailMsg += "The target " + currentTargetName + " is in process for : " + currentTargetSummary.currentProcessingTime().toMillis() + " m/s";
//                    break;
//                }
//                case Finished:
//                {
//                    Duration time = currentTargetSummary.getTime();
//                    detailMsg += "Target's result status : ";
//
//                    if(currentTargetSummary.isSkipped())
//                        detailMsg += "Skipped\n";
//                    else
//                        detailMsg += currentTargetSummary.getResultStatus() + "\n";
//
//                    if(!currentTargetSummary.isSkipped())
//                        detailMsg += "Target's running time: " + time.toMillis() + "m/s\n";
//                    break;
//                }
//            }
//        }
//
//        this.taskDetailsOnTargetTextArea.setText(detailMsg);
    }

    public String printTargetWaitingForTargets(String currentTargetName) {
        String waitingForTargets = "", dependedOnTarget;
        Set<String> dependedTargets = this.graph.getTarget(currentTargetName).getAllDependsOnTargets();

        for(TaskTargetCurrentInfoTableItem curr : this.taskTargetDetailsTableView.getItems())
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

    public String printProcessedFailedTargets(String currentTargetName) {
        String processedFailedTargets = "", dependedOnTarget;
        Set<String> dependedTargets = this.graph.getTarget(currentTargetName).getAllDependsOnTargets();

        for(TaskTargetCurrentInfoTableItem curr : this.taskTargetDetailsTableView.getItems())
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

    public void getFinishedTargetsInRealTime() {
        this.finishedTargets = 0;
        for(TaskTargetCurrentInfoTableItem currItem : this.taskTargetDetailsTableView.getItems())
        {
            if(currItem.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Finished.toString())||currItem.getRuntimeStatus().equals(TargetSummary.RuntimeStatus.Skipped.toString()))
                ++this.finishedTargets;
        }
    }

    //------------------------------------------Preparations For Launch-------------------------------------------//
    private boolean incrementalIsOptional() {
        if(this.isTaskRunning)
            return false;

        for(TaskTargetCurrentInfoTableItem curr : this.taskTargetDetailsTableView.getItems())
            if(curr.getResultStatus().equalsIgnoreCase("Undefined") || curr.getResultStatus().equalsIgnoreCase("Skipped")) //At least one of the targets in not finished or skipped
                return false;

        return true;
    }

    private void turnOnIncrementalButton() {
        boolean change = false;

        for(TaskTargetCurrentInfoTableItem curr : this.taskTargetDetailsTableView.getItems())
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

    private void createNewProgressBar() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int maxSize = AdminTaskControlController.this.taskTargetDetailsTableView.getItems().size();
                while (true) {
                    Thread.sleep(200);
                    getFinishedTargetsInRealTime();
                    updateProgress(AdminTaskControlController.this.finishedTargets, maxSize);
                }
//                updateProgress(maxSize, maxSize);
//                return null;
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
    @FXML void runPressed(ActionEvent event) {
        this.runButton.setDisable(true);
        disablePauseAndStopButtons(true);
        sendRequestToStartTask();
    }

    private void sendRequestToStartTask() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("start-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                {
                    AdminTaskControlController.this.runButton.setDisable(false);
                    ShowPopup(Alert.AlertType.ERROR, "Failure In Starting Task!", null,
                            "Failure on connecting to server for starting task!");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.isTaskRunning = true;
                        disablePauseAndStopButtons(false);
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Started Successfully!", null, message);
                    });
                else //Failed
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.runButton.setDisable(false);
                        ShowPopup(Alert.AlertType.ERROR, "Failure In Starting Task!", null, message);
                    });

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    @FXML void pausePressed(ActionEvent event) {
        disablePauseAndStopButtons(true);

        if (this.PauseButton.getText().equalsIgnoreCase("Pause"))
            sendRequestToPauseTask();
        else //Resume
            sendRequestToResumeTask();
    }

    private void disablePauseAndStopButtons(boolean flag) {
        this.PauseButton.setDisable(flag);
        this.stopButton.setDisable(flag);
    }

    private void sendRequestToPauseTask() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("pause-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for pausing task!"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.PauseButton.setText("Resume");
                        disablePauseAndStopButtons(false);
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Paused Successfully!", null, message);
                    });
                else //Failed
                    Platform.runLater(() -> ShowPopup(Alert.AlertType.ERROR, "Failure In Pausing Task!", null, message));

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void sendRequestToResumeTask() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("resume-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for resuming task!"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.PauseButton.setText("Pause");
                        disablePauseAndStopButtons(false);
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Resumed Successfully!", null, message);
                    });
                else //Failed
                    Platform.runLater(() -> ShowPopup(Alert.AlertType.ERROR, "Failure In Resuming Task!", null, message));

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    @FXML void stopPressed(ActionEvent event) {
        disablePauseAndStopButtons(true);
        sendRequestToStopTask();
    }

    private void sendRequestToStopTask() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("stop-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for stopping task!"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.isTaskRunning = false;
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Stopped Successfully!", null, message);
                    });
                else //Failed
                    Platform.runLater(() ->
                        ShowPopup(Alert.AlertType.ERROR, "Failure In Stopping Task!", null, message));

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void updateTableRuntimeStatuses() {
        ObservableList<TaskTargetCurrentInfoTableItem> itemsList = this.taskTargetDetailsTableView.getItems();
        LocalTime startTime = LocalTime.now();
        LocalTime currTime = LocalTime.now();

        while (true)
        {
            startTime = LocalTime.now();
            updateTable(itemsList, startTime, currTime);
        }
//        updateTable(itemsList, startTime, currTime);
//        AdminTaskControlController.this.incrementalRadioButton.setDisable(!incrementalIsOptional());
    }

    public void updateTable(ObservableList<TaskTargetCurrentInfoTableItem> itemsList , LocalTime startTime, LocalTime currTime) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (TaskTargetCurrentInfoTableItem item : itemsList)
        {
            item.setRuntimeStatus(this.graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getRuntimeStatus().toString());
            item.setResultStatus(this.graphSummary.getTargetsSummaryMap().get(item.getTargetName()).getResultStatus().toString());
        }
        Platform.runLater(()->{this.taskTargetDetailsTableView.refresh();});
    }

    //----------------------------------------------------Other---------------------------------------------------//
    public static void ShowPopup(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



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