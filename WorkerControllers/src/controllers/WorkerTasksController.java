package controllers;

import com.google.gson.Gson;
import dtos.WorkerChosenTargetDTO;
import dtos.WorkerChosenTaskDTO;
import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import tableItems.WorkerChosenTargetInformationTableItem;
import tableItems.WorkerChosenTaskInformationTableItem;
import task.CompilationThread;
import task.SimulationThread;
import task.WorkerCompilationParameters;
import task.WorkerSimulationParameters;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerTasksController {
    //------------------------------------------------- Members ----------------------------------------------------//
    private final ObservableList<String> historyOfTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> registeredTasksList = FXCollections.observableArrayList();
    private final ObservableList<WorkerChosenTargetInformationTableItem> chosenTargetInfoList = FXCollections.observableArrayList();
    private final ObservableList<WorkerChosenTaskInformationTableItem> chosenTaskInfoList = FXCollections.observableArrayList();
    private final Set<String> pausedTasks = new HashSet<>();
    private TasksPullerThread tasksPullerThread;
    public ProgressBar progressBar;
    public Label ProgressPercentageLabel;
    private String userName;
    private String chosenTask;
    private String chosenTarget;
    private Integer numOfThreads;
    private ThreadPoolExecutor executor;
    private Integer totalTargets = 1;
    private Integer finishedTargets = 0;
    private final Random random = new Random();

    //----------------------------------------------- FXML Members -------------------------------------------------//
    @FXML private ListView<String> TargetsListView;
    @FXML private ListView<String> TasksListView;
    @FXML private TableView<WorkerChosenTargetInformationTableItem> TargetTableView;
    @FXML private TableColumn<WorkerChosenTargetInformationTableItem, String> Target;
    @FXML private TableColumn<WorkerChosenTargetInformationTableItem, String> Task;
    @FXML private TableColumn<WorkerChosenTargetInformationTableItem, String> TaskType;
    @FXML private TableColumn<WorkerChosenTargetInformationTableItem, String> SelectedTargetStatus;
    @FXML private TableColumn<WorkerChosenTargetInformationTableItem, Integer> SelectedTargetEarnedCredits;
    @FXML private TextArea TargetLogTextArea;
    @FXML private TableView<WorkerChosenTaskInformationTableItem> TaskTableView;
    @FXML private TableColumn<WorkerChosenTaskInformationTableItem, String> Name;
    @FXML private TableColumn<WorkerChosenTaskInformationTableItem, String> SelectedTaskStatus;
    @FXML private TableColumn<WorkerChosenTaskInformationTableItem, Integer> Workers;
    @FXML private TableColumn<WorkerChosenTaskInformationTableItem, Integer> FinishedTargets;
    @FXML private TableColumn<WorkerChosenTaskInformationTableItem, Integer> SelectedTaskEarnedCredits;
    @FXML private Button PauseButton;
    @FXML private Button LeaveTaskButton;

    //------------------------------------------------- Settings ----------------------------------------------------//
    public void initialize(String userName, Integer numOfThreads) {
        setUserName(userName);
        setNumOfThreads(numOfThreads);
        initializeListViews();
        initializeChosenTargetTaskTable();
        initializeChosenTaskTable();
        setupListeners();
        createTaskPullerThread();
        createThreadPool();
    }

    private void initializeListViews() {
        this.TasksListView.setItems(this.registeredTasksList);
        this.TargetsListView.setItems(this.historyOfTargetsList);
    }

    private void setUserName(String userName) { this.userName = userName; }

    private void setNumOfThreads(Integer numOfThreads) { this.numOfThreads = numOfThreads; }

    private void createTaskPullerThread() {
        this.tasksPullerThread = new TasksPullerThread();
        this.tasksPullerThread.setDaemon(true);
        this.tasksPullerThread.start();
    }

    private void createThreadPool() {
        this.executor = new ThreadPoolExecutor(this.numOfThreads, this.numOfThreads, 1000000,
                TimeUnit.MINUTES, new LinkedBlockingDeque<>());
    }

    public void initializeChosenTargetTaskTable() {
        this.Target.setCellValueFactory(new PropertyValueFactory<WorkerChosenTargetInformationTableItem, String>("targetName"));
        this.Task.setCellValueFactory(new PropertyValueFactory<WorkerChosenTargetInformationTableItem, String >("taskName"));
        this.TaskType.setCellValueFactory(new PropertyValueFactory<WorkerChosenTargetInformationTableItem, String >("taskType"));
        this.SelectedTargetStatus.setCellValueFactory(new PropertyValueFactory<WorkerChosenTargetInformationTableItem, String >("status"));
        this.SelectedTargetEarnedCredits.setCellValueFactory(new PropertyValueFactory<WorkerChosenTargetInformationTableItem, Integer>("earnedCredits"));
    }

    public void initializeChosenTaskTable() {
        this.Name.setCellValueFactory(new PropertyValueFactory<WorkerChosenTaskInformationTableItem, String>("name"));
        this.SelectedTaskStatus.setCellValueFactory(new PropertyValueFactory<WorkerChosenTaskInformationTableItem, String >("status"));
        this.Workers.setCellValueFactory(new PropertyValueFactory<WorkerChosenTaskInformationTableItem, Integer>("workers"));
        this.FinishedTargets.setCellValueFactory(new PropertyValueFactory<WorkerChosenTaskInformationTableItem, Integer>("finishedTargets"));
        this.SelectedTaskEarnedCredits.setCellValueFactory(new PropertyValueFactory<WorkerChosenTaskInformationTableItem, Integer>("earnedCredits"));
    }

    private void setupListeners() {
        this.registeredTasksList.addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!WorkerTasksController.this.TasksListView.getItems().contains(curr))
                        WorkerTasksController.this.TasksListView.getItems().add(curr);
                }
            }
        });

        this.historyOfTargetsList.addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!WorkerTasksController.this.TargetsListView.getItems().contains(curr))
                        WorkerTasksController.this.TargetsListView.getItems().add(curr);
                }
            }
        });
    }

    //------------------------------------------------- During Task ------------------------------------------------//
    public void PauseButtonPressed() {
        if(this.PauseButton.getText().equals("Pause"))
        {
            this.pausedTasks.add(this.chosenTask);
            this.PauseButton.setText("Resume");
        }
        else
        {
            this.pausedTasks.remove(this.chosenTask);
            this.PauseButton.setText("Pause");
        }
//        sendPausedTaskRequestToServer();
    }

//    private void sendPausedTaskRequestToServer() {
//        String finalUrl = HttpUrl
//                .parse(Patterns.TASK_UPDATE)
//                .newBuilder()
//                .addQueryParameter("worker-pause-task", WorkerTasksController.this.chosenTask)
//                .addQueryParameter("username", this.userName)
//                .build()
//                .toString();
//
//        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {
//            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Platform.runLater(() -> System.out.println("Failure on connecting to server for worker-pause-task!"));
//            }
//
//            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
//                String message = response.header("message");
//
//                if (response.code() >= 200 && response.code() < 300) //Success
//                    Platform.runLater(() ->
//                    {
//                        WorkerTasksController.this.PauseButton.setText("Resume");
//                        ShowPopUp(Alert.AlertType.INFORMATION, "Task paused!", null, message);
//                    });
//                else //Failure
//                    Platform.runLater(() -> System.out.println(message));
//
//                WorkerTasksController.this.PauseButton.setDisable(false);
//                Objects.requireNonNull(response.body()).close();
//            }
//        });
//    }

    public void LeaveTaskButtonPressed() {
        if(confirmLeavingTheTask())
            leaveTheTask();
    }

    private boolean confirmLeavingTheTask() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Confirmation");
        alert.setHeaderText("Are you sure you want to leave the task?");

        alert.initOwner(this.LeaveTaskButton.getScene().getWindow());
        Toolkit.getDefaultToolkit().beep();
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    private void leaveTheTask()
    {
        setTaskControlButtons(true);
        this.registeredTasksList.remove(this.chosenTask);
        sendUnregisterRequestToServer();
    }

    private void setTaskControlButtons(Boolean flag) {
        this.LeaveTaskButton.setDisable(flag);
        this.PauseButton.setDisable(flag);
    }

    //-------------------------------------------------- Register --------------------------------------------------//
    private void sendUnregisterRequestToServer() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_REGISTER)
                .newBuilder()
                .addQueryParameter("unregister", this.chosenTask)
                .addQueryParameter("username", this.userName)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {

            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Error", null, e.getMessage()));}

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");
                String taskName = response.header("task");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(()->
                    {
                        WorkerTasksController.this.pausedTasks.remove(taskName);
                        ShowPopUp(Alert.AlertType.INFORMATION, "Unregistration Successfully!", null, message);
                    });
                else //Failed
                    Platform.runLater(()->
                    {
                        WorkerTasksController.this.LeaveTaskButton.setDisable(false);
                        ShowPopUp(Alert.AlertType.ERROR, "Unregistration Failed!", null, message);
                    });

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void refreshChosenTargetInfo(WorkerChosenTargetDTO dto) {
        refreshChosenTargetTable(dto.getItem());
        refreshChosenTargetLog(dto.getLog());
    }

    private void refreshChosenTargetTable(WorkerChosenTargetInformationTableItem item) {
        this.chosenTargetInfoList.clear();
        this.chosenTargetInfoList.add(item);

        this.TargetTableView.setItems(this.chosenTargetInfoList);
    }

    private void refreshChosenTargetLog(String log) {
        if(log != null)
        {
            this.TargetLogTextArea.clear();
            this.TargetLogTextArea.appendText(log);
        }
    }

    //------------------------------------------------- Task Info --------------------------------------------------//
    public void getInfoAboutSelectedTaskFromListView() {
        String selectedTaskName = WorkerTasksController.this.TasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName != null)
            WorkerTasksController.this.chosenTask = selectedTaskName;
        else if(WorkerTasksController.this.chosenTask == null)
            return;

        this.tasksPullerThread.sendChosenTaskUpdateRequestToServer();

        setTaskControlButtons(false);
        Platform.runLater(() -> this.PauseButton.setText(this.pausedTasks.contains(this.chosenTask) ? "Resume" : "Pause"));
    }

    private void refreshChosenTaskTable(WorkerChosenTaskInformationTableItem item) {
        this.chosenTaskInfoList.clear();
        this.chosenTaskInfoList.add(item);

        this.TaskTableView.setItems(this.chosenTaskInfoList);
    }

    private void chosenTaskRemovedFromListView() {
        WorkerTasksController.this.chosenTask = null;
        setTaskControlButtons(true);
        resetChosenTaskTableView();
        resetProgressBar();
    }

    private void resetChosenTaskTableView() {
        this.chosenTaskInfoList.clear();
    }

    //------------------------------------------------ Target Info -------------------------------------------------//
    public void getInfoAboutSelectedTargetFromListView() {
        String selectedTargetName = WorkerTasksController.this.TargetsListView.getSelectionModel().getSelectedItem();

        if(selectedTargetName != null)
            WorkerTasksController.this.chosenTarget = selectedTargetName;
        else if(this.chosenTarget == null)
            return;

        this.tasksPullerThread.sendChosenTargetUpdateRequestToServer();
    }


    //----------------------------------------------- Puller Thread ------------------------------------------------//
    public class TasksPullerThread extends Thread {
        @Override public void run()
        {
            createNewProgressBar();

            while(this.isAlive())
            {
                sendingThreadToSleep();
                getRegisteredTasks();
                getInfoAboutSelectedTargetFromListView();
                getInfoAboutSelectedTaskFromListView();
                getTargetsToExecute();
                getExecutedTargets();
            }
        }

        //--------------------------- Sleep -----------------------------//
        private void sendingThreadToSleep() {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //----------------------- Registered Tasks ----------------------//
        public void getRegisteredTasks() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("registered-tasks", WorkerTasksController.this.userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for registered-tasks!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set registeredTasks = new Gson().fromJson(body, Set.class);
                            refreshTasksListView(registeredTasks);
                            refreshPausedTasksSet(registeredTasks);
                        });
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull registered tasks from server!"));

                Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshPausedTasksSet(Set<String> registeredTasks) {
            if(registeredTasks == null)
                return;

            WorkerTasksController.this.pausedTasks.removeIf(pausedTask -> !registeredTasks.contains(pausedTask));
        }

        //----------------------- Chosen Task Info ----------------------//
        private void refreshTasksListView(Set<String> registered) {
            if (registered == null)
                return;

            WorkerTasksController.this.registeredTasksList.removeIf(curr -> !registered.contains(curr));

            if(!registered.contains(WorkerTasksController.this.chosenTask))
                chosenTaskRemovedFromListView();

            for(String curr : registered)
            {
                if(!WorkerTasksController.this.registeredTasksList.contains(curr))
                    WorkerTasksController.this.registeredTasksList.add(curr);
            }

            WorkerTasksController.this.PauseButton.setText(WorkerTasksController.this.pausedTasks.contains(WorkerTasksController.this.chosenTask) ? "Resume" : "Pause");
        }

        private void sendChosenTargetUpdateRequestToServer() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("chosen-target", WorkerTasksController.this.chosenTarget)
                    .addQueryParameter("username", WorkerTasksController.this.userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for chosen-target!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            WorkerChosenTargetDTO dto = new Gson().fromJson(body, WorkerChosenTargetDTO.class);
                            refreshChosenTargetInfo(dto);
                        });
                    } else Platform.runLater(() -> System.out.println("couldn't pull chosen-target from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void sendChosenTaskUpdateRequestToServer() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("chosen-task", WorkerTasksController.this.chosenTask)
                    .addQueryParameter("username", WorkerTasksController.this.userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for chosen-task!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            WorkerChosenTaskDTO dto = new Gson().fromJson(body, WorkerChosenTaskDTO.class);
                            refreshChosenTaskTable(dto.getItem());
                            refreshProgressBar(dto.getTotalTargets(), dto.getFinishedTargets());
                        });
                    } else Platform.runLater(() -> System.out.println("couldn't pull chosen-task from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        //---------------------- Executed Targets -----------------------//
        private void getExecutedTargets()
        {
            sendExecutedTargetsRequestToServer();
        }

        private void sendExecutedTargetsRequestToServer() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("executed-targets", WorkerTasksController.this.userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for executed targets history!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set targets = new Gson().fromJson(body, Set.class);
                            refreshTargetsListView(targets);
                        });
                    }

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshTargetsListView(Set<String> targets) {
            if (targets == null)
                return;

            for(String curr : targets)
            {
                if(!WorkerTasksController.this.historyOfTargetsList.contains(curr))
                    WorkerTasksController.this.historyOfTargetsList.add(curr);
            }
        }

        //---------------------- Executing Targets ----------------------//
        private void getTargetsToExecute() {
            if(!isThreadPoolFull() && !WorkerTasksController.this.registeredTasksList.isEmpty())
                sendTargetsToExecuteRequestToServer();
        }

        private Boolean isThreadPoolFull() {
            return WorkerTasksController.this.executor.getActiveCount() == WorkerTasksController.this.numOfThreads;
        }

        private void sendTargetsToExecuteRequestToServer() {
            String taskName = getTaskToExecute();

            if(taskName == null)
                return;

            String finalUrl = HttpUrl
                    .parse(Patterns.TASK)
                    .newBuilder()
                    .addQueryParameter("execute", taskName)
                    .addQueryParameter("username", WorkerTasksController.this.userName)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for executing targets!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            try {
                                if(Objects.equals(response.header("task-type"), "Simulation"))
                                    executeSimulationTarget(body);
                                else //Compilation
                                    executeCompilationTarget(body);
                            } catch (Exception e) {System.out.println("Error in pulling task:" + e.getMessage()); }
                        });
                    }

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private String getTaskToExecute() {
            String taskName = null;
            boolean validTask = false;
            int index;

            while(!validTask)
            {
                try {
                    index = WorkerTasksController.this.random.nextInt(WorkerTasksController.this.registeredTasksList.size());
                    taskName = WorkerTasksController.this.registeredTasksList.get(index);

                    if(!WorkerTasksController.this.pausedTasks.contains(taskName))
                        validTask = true;
                    else if(WorkerTasksController.this.registeredTasksList.size() == WorkerTasksController.this.pausedTasks.size())
                        return null;
                }
                catch(Exception e) { return null; }
            }

            return taskName;
        }

        private void executeSimulationTarget(String body) {
            WorkerSimulationParameters parameters = new Gson().fromJson(body, WorkerSimulationParameters.class);
            WorkerTasksController.this.executor.execute(new SimulationThread(parameters));
        }

        private void executeCompilationTarget(String body) {
            WorkerCompilationParameters parameters = new Gson().fromJson(body, WorkerCompilationParameters.class);
            WorkerTasksController.this.executor.execute(new CompilationThread(parameters));
        }
    }

    //----------------------------------------------- Progress Bar -------------------------------------------------//
    private void createNewProgressBar() {
        javafx.concurrent.Task<Void> task = new Task<Void>() {
            @Override protected Void call() {
                while (true) {
                    updateProgress(WorkerTasksController.this.finishedTargets, WorkerTasksController.this.totalTargets);
                }
            }
        };

        WorkerTasksController.this.progressBar.setStyle("-fx-accent: #00FF00;");
        WorkerTasksController.this.progressBar.progressProperty().bind(task.progressProperty());
        WorkerTasksController.this.ProgressPercentageLabel.textProperty().bind
                (Bindings.concat(Bindings.format("%.0f", Bindings.multiply(task.progressProperty(), 100)), " %"));

        Thread progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);
        progressBarThread.start();
    }

    private void resetProgressBar() {
        this.finishedTargets = 0;
        this.totalTargets = 1;
    }

    private void refreshProgressBar(Integer totalTargets, Integer finishedTargets) {
        this.totalTargets = totalTargets;
        this.finishedTargets = finishedTargets;
    }

    //------------------------------------------------- General ----------------------------------------------------//
    public static void ShowPopUp(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}