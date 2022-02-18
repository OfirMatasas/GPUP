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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import tableItems.TaskTargetCurrentInfoTableItem;

import java.io.IOException;
import java.util.Objects;

public class AdminTaskControlController {
    //------------------------------------------------- Members ----------------------------------------------------//
    private ObservableList<TaskTargetCurrentInfoTableItem> taskTargetStatusesList = FXCollections.observableArrayList();
    private Integer finishedTargets = 0;
    private Integer totalTargets = 1;
    private String taskName = null;
    private String userName;
    private Boolean isTaskRunning = false;
    private TaskControlPullerThread taskControlPullerThread;
    private Boolean isFirstRun;
    private String chosenTarget = null;

    //---------------------------------------------- FXML Members --------------------------------------------------//
    @FXML private Button runButton;
    @FXML private Button PauseButton;
    @FXML private Button stopButton;
    @FXML private RadioButton fromScratchRadioButton;
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
    @FXML private Label progressBarPercentage;
    @FXML private TextArea logTextArea;

    //------------------------------------------------ Puller Thread -----------------------------------------------//
    public class TaskControlPullerThread extends Thread {
        @Override public void run()
        {
            createNewProgressBar();

            while(this.isAlive())
            {
                sendingThreadToSleep();

                if(AdminTaskControlController.this.isTaskRunning)
                    getTargetCurrentInfo();

                sendChosenTargetRunningInfoRequestToServer();
            }
        }

        private void sendingThreadToSleep() {
            try {
                sleep(500);
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
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for task-update!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                            {
                                AllTaskDetails updatedInfo = new Gson().fromJson(body, AllTaskDetails.class);
                                refreshInfo(updatedInfo);
                                updateProgressBar(updatedInfo);
                            });
                    } else //Failure
                    {
                        String message = response.header("message");
                        Platform.runLater(() -> System.out.println("couldn't pull task update from server!\n" + message));
                    }

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
                            AdminTaskControlController.this.isFirstRun = false;
                            AdminTaskControlController.this.isTaskRunning = false;
                            AdminTaskControlController.this.runButton.setDisable(false);
                            disablePauseAndStopButtons(true);
                            ShowPopup(Alert.AlertType.INFORMATION, "Task Finished!", null, message);
                        });
                    }
                }
            });
        }

        private void updateProgressBar(AllTaskDetails updatedInfo) {
            AdminTaskControlController.this.finishedTargets = updatedInfo.getFinishedTargets();
            AdminTaskControlController.this.totalTargets = updatedInfo.getTargets();
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

    //------------------------------------------------ Initialize ----------------------------------------------//
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

    //-------------------------------------------- Task information ----------------------------------------------//
    public void setTaskStaticInformation(String taskName, String graphName) {
        this.TaskNameTextField.setText(taskName);
        this.GraphNameTextField.setText(graphName);
    }

    //------------------------------------------- Target information ---------------------------------------------//
    @FXML void getSelectedRow() {
        TaskTargetCurrentInfoTableItem selectedItem = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();

        if(selectedItem == null)
        {
            enableTargetInfoTextArea(false);
            return;
        }

        this.chosenTarget = selectedItem.getTargetName();
        sendChosenTargetRunningInfoRequestToServer();
        enableTargetInfoTextArea(true);
    }

    private void sendChosenTargetRunningInfoRequestToServer() {
        if(this.chosenTarget == null)
            return;

        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("admin-target-info", this.chosenTarget)
                .addQueryParameter("task", AdminTaskControlController.this.taskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for target info!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String message;
                String body;
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    body = Objects.requireNonNull(response.body()).string();

                    String log = new Gson().fromJson(body, String.class);
                    Platform.runLater(() -> showDetailsOfSelectedTargetInTextArea(log));
                }
                else //Failed
                {
                    message = response.header("message");
                    Platform.runLater(() -> ShowPopup(Alert.AlertType.ERROR, "Failure In Pulling Target Info!", null, message));
                }

                Objects.requireNonNull(response.body()).close();
            }
        });

    }

    private void enableTargetInfoTextArea(boolean flag) {
        this.taskDetailsOnTargetTextArea.setVisible(flag);
        this.taskDetailsOnTargetTextArea.setDisable(!flag);
    }

    public void showDetailsOfSelectedTargetInTextArea(String targetRunningInfo) {
        this.taskDetailsOnTargetTextArea.setText(targetRunningInfo);
    }

    //----------------------------------------- Preparations For Launch ------------------------------------------//
    private boolean incrementalIsOptional() {
        if(this.isTaskRunning)
            return false;

        return !this.isFirstRun;
    }

    //----------------------------------------------- Progress Bar -----------------------------------------------//
    private void turnOnProgressBar() {
        this.progressBar.setDisable(false);
        this.progressBarPercentage.setDisable(false);
        this.targetsFinishedLabel.setDisable(false);
    }

    private void createNewProgressBar() {
        javafx.concurrent.Task<Void> task = new Task<Void>() {
            @Override protected Void call() {
                while (true) {
                    updateProgress(AdminTaskControlController.this.finishedTargets, AdminTaskControlController.this.totalTargets);
                }
            }
        };

        this.progressBar.setStyle("-fx-accent: #00FF00;");
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressBarPercentage.textProperty().bind
                (Bindings.concat(Bindings.format("%.0f", Bindings.multiply(task.progressProperty(), 100)), " %"));

        Thread progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);
        progressBarThread.start();
    }

    //------------------------------------------------- During Task ------------------------------------------------//
    //----------------------------- Run Task -----------------------------//
    @FXML void runPressed(ActionEvent event) {
        this.runButton.setDisable(true);
        disablePauseAndStopButtons(true);

        if(this.fromScratchRadioButton.isSelected()) //From scratch
            sendRequestToStartTaskFromScratch();
        else //Incremental
            sendRequestToStartTaskIncrementally();
    }

    private void sendRequestToStartTaskFromScratch() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("start-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                {
                    AdminTaskControlController.this.runButton.setDisable(false);
                    ShowPopup(Alert.AlertType.ERROR, "Failure In Starting Task!", null,
                            "Failure on connecting to server for starting task!");
                });
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");
                String returnedTaskName = response.header("task-name");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        if(returnedTaskName != null){
                            AdminTaskControlController.this.taskName = returnedTaskName;
                            AdminTaskControlController.this.TaskNameTextField.setText(returnedTaskName);
                        }

                        AdminTaskControlController.this.isTaskRunning = true;
                        disablePauseAndStopButtons(false);
                        turnOnProgressBar();
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Started Successfully!", null, message);
                    });
                else //Failure
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.runButton.setDisable(false);
                        ShowPopup(Alert.AlertType.ERROR, "Failure In Starting Task!", null, message);
                    });

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void sendRequestToStartTaskIncrementally() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_UPDATE)
                .newBuilder()
                .addQueryParameter("start-task", AdminTaskControlController.this.taskName)
                .addQueryParameter("username", this.userName)
                .addQueryParameter("incremental", "yes")
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                {
                    AdminTaskControlController.this.runButton.setDisable(false);
                    ShowPopup(Alert.AlertType.ERROR, "Failure In Run Task Incrementally!", null,
                            "Failure on connecting to server for running incrementally!");
                });
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");
                String returnedTaskName = response.header("task-name");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        if(returnedTaskName != null)
                        {
                            AdminTaskControlController.this.taskName = returnedTaskName;
                            AdminTaskControlController.this.TaskNameTextField.setText(returnedTaskName);
                        }

                        AdminTaskControlController.this.isTaskRunning = true;
                        disablePauseAndStopButtons(false);
                        turnOnProgressBar();
                        resetTaskTargetDetailsTableView();
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Copied Successfully!", null, message);
                    });
                else //Failure
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.runButton.setDisable(false);
                        ShowPopup(Alert.AlertType.ERROR, "Failure In Copying Task!", null, message);
                    });

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void resetTaskTargetDetailsTableView() {
        this.taskTargetStatusesList = FXCollections.observableArrayList(this.taskTargetStatusesList
                .filtered(p -> p.getResultStatus().equalsIgnoreCase("Success") ||
                    p.getResultStatus().equalsIgnoreCase("Warning")));
        this.taskTargetDetailsTableView.setItems(this.taskTargetStatusesList);
    }

    //---------------------------- Pause Task -----------------------------//
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
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for pausing task!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
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
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for resuming task!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
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

    //---------------------------- Stop Task -----------------------------//
    @FXML void stopPressed() {
        this.isTaskRunning = false;
        this.isFirstRun = false;
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
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for stopping task!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.runButton.setDisable(false);
                        ShowPopup(Alert.AlertType.INFORMATION, "Task Stopped Successfully!", null, message);
                    });
                else //Failure
                    Platform.runLater(() ->
                    {
                        AdminTaskControlController.this.isTaskRunning = true;
                        ShowPopup(Alert.AlertType.ERROR, "Failure In Stopping Task!", null, message);
                    });

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    //---------------------------------------------------- General -------------------------------------------------//
    public static void ShowPopup(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}