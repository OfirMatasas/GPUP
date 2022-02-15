package controllers;

import com.google.gson.Gson;
import information.AllTaskDetails;
import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import tableItems.SelectedGraphTableItem;
import tableItems.WorkerTaskStatusTableItem;
import users.UsersLists;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import static controllers.WorkerPrimaryController.ShowPopUp;

public class WorkerDashboardController {
    //----------------------------------------------- My Members -------------------------------------------//
    private final ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private final ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private final ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private final ObservableList<SelectedGraphTableItem> selectedTaskTargetsList = FXCollections.observableArrayList();
    private final ObservableList<WorkerTaskStatusTableItem> selectedTaskStatusList = FXCollections.observableArrayList();
    private WorkerPrimaryController workerPrimaryController;
    private String username;
    private DashboardPullerThread dashboardPullerThread;
    private String chosenTask = null;
    private boolean register = true;

    //---------------------------------------------- FXML Members -------------------------------------------//
    @FXML private SplitPane SplitPane;
    @FXML private TitledPane OnlineTasksTiltedPane;
    @FXML private ListView<String> TasksListView;
    @FXML private Button RegisterToTaskButton;
    @FXML private TitledPane AdminsListView;
    @FXML private ListView<String> onlineAdminsListView;
    @FXML private TitledPane OnlineWorkersTiltedPane;
    @FXML private ListView<String> onlineWorkersListView;
    @FXML private Font x11;
    @FXML private Color x21;
    @FXML private TextField UserNameTextField;
    @FXML private TextField TotalCreditsTextField;
    @FXML private Font x1;
    @FXML private Color x2;
    @FXML private TextField TaskNameTextField;
    @FXML private TextField CreatedByTextField;
    @FXML private TableView<SelectedGraphTableItem> TaskTargetsTableView;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> Targets;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> Independents;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> Leaves;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> Middles;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> Roots;
    @FXML private TableView<WorkerTaskStatusTableItem> TaskStatusTableView;
    @FXML private TableColumn<WorkerTaskStatusTableItem, String> Type;
    @FXML private TableColumn<WorkerTaskStatusTableItem, String> Status;
    @FXML private TableColumn<WorkerTaskStatusTableItem, Integer> Workers;
    @FXML private TableColumn<WorkerTaskStatusTableItem, Integer> Payment;
    @FXML private TableColumn<WorkerTaskStatusTableItem, String> Registered;

    //---------------------------------------------- Initialize -------------------------------------------//
    public void initialize(WorkerPrimaryController workerPrimaryController, String userName)
    {
        initializeTaskTargetDetailsTable();
        initializeTaskDetailsTable();
        setupListeners();
        createPullingThread();
        applyUserName(userName);

        this.workerPrimaryController = workerPrimaryController;
    }

    private void applyUserName(String userName) {
        this.username = userName;
        this.UserNameTextField.setText(userName);
    }

    public void initializeTaskTargetDetailsTable() {
        this.Targets.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("targets"));
        this.Roots.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("roots"));
        this.Middles.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("middles"));
        this.Leaves.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("leaves"));
        this.Independents.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("independents"));
    }

    public void initializeTaskDetailsTable() {
        this.Type.setCellValueFactory(new PropertyValueFactory<WorkerTaskStatusTableItem, String>("type"));
        this.Status.setCellValueFactory(new PropertyValueFactory<WorkerTaskStatusTableItem, String>("status"));
        this.Workers.setCellValueFactory(new PropertyValueFactory<WorkerTaskStatusTableItem, Integer>("workers"));
        this.Payment.setCellValueFactory(new PropertyValueFactory<WorkerTaskStatusTableItem, Integer>("totalPayment"));
        this.Registered.setCellValueFactory(new PropertyValueFactory<WorkerTaskStatusTableItem, String>("registered"));
    }

    private void setupListeners() {
        this.onlineTasksList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!WorkerDashboardController.this.TasksListView.getItems().contains(curr))
                        WorkerDashboardController.this.TasksListView.getItems().add(curr);
                }

                WorkerDashboardController.this.TasksListView.getItems().removeIf(curr -> !c.getList().contains(curr));
            }
        });
    }

    //--------------------------------------------- Puller Thread ------------------------------------------//
    public class DashboardPullerThread extends Thread
    {
        @Override public void run()
        {
            while(true)
            {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getOnlineUsersFromServer();
                getOnlineTasksFromServer();
                getWorkerCreditsFromServer();

                if(WorkerDashboardController.this.chosenTask != null)
                    TaskSelectedFromTaskListView();
            }
        }

        //-------------------------- Credits -------------------------//
        private void getWorkerCreditsFromServer() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_UPDATE)
                    .newBuilder()
                    .addQueryParameter("credits", WorkerDashboardController.this.username)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for worker's credits!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String credits = response.header("credits");
                        Platform.runLater(() ->
                                WorkerDashboardController.this.TotalCreditsTextField.setText(credits));
                    }
                    else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull worker's credits from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }
        //------------------------ Tasks List ------------------------//
        private void getOnlineTasksFromServer() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_LIST)
                    .newBuilder()
                    .addQueryParameter("active-tasks-list", "active-tasks-list")
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for task-list!"));
                }

                @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set taskList = new Gson().fromJson(body, Set.class);
                            refreshTasksList(taskList);
                        });
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull all-task-list from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshTasksList(Set<String> taskList)
        {
            if(taskList == null)
                return;

            for(String curr : taskList) {
                if (!WorkerDashboardController.this.onlineTasksList.contains(curr))
                    WorkerDashboardController.this.onlineTasksList.add(curr);
            }

            WorkerDashboardController.this.onlineTasksList.removeIf(curr -> !taskList.contains(curr));

            if(!taskList.contains(WorkerDashboardController.this.chosenTask))
            {
                WorkerDashboardController.this.chosenTask = null;
                resetRegisterToTaskButton();
                resetTableViews();
            }
        }
    }

    private void resetRegisterToTaskButton() {
        this.RegisterToTaskButton.setText("Register To Task");
        this.RegisterToTaskButton.setDisable(true);
    }

    private void resetTableViews() {
        this.selectedTaskStatusList.clear();
        this.selectedTaskTargetsList.clear();
    }

    //------------------------ Users List ------------------------//
    private void getOnlineUsersFromServer() {
        String finalUrl = HttpUrl
                .parse(Patterns.USER_LIST)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for users list!"));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = Objects.requireNonNull(response.body()).string();
                UsersLists usersLists = new Gson().fromJson(body, UsersLists.class);

                Platform.runLater(() -> updateUsersLists(usersLists));
                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void updateUsersLists(UsersLists usersLists) {
        this.onlineAdminsList.clear();
        this.onlineWorkersList.clear();
        this.onlineAdminsList.addAll(usersLists.getAdminsList());
        this.onlineWorkersList.addAll(usersLists.getWorkersList());
    }

    private void createPullingThread() {
        this.dashboardPullerThread = new DashboardPullerThread();

        this.onlineAdminsListView.setItems(this.onlineAdminsList);
        this.onlineWorkersListView.setItems(this.onlineWorkersList);

        this.dashboardPullerThread.setDaemon(true);
        this.dashboardPullerThread.start();
    }

    //----------------------------------------- Task Registration  --------------------------------------------//
    @FXML void RegisterToTaskButtonPressed(ActionEvent event) {
        String selectedTaskName = this.TasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        String registerRequest = this.register ? "register" : "unregister";
        sendRegisterRequestToServer(registerRequest, selectedTaskName);
    }

    private void sendRegisterRequestToServer(String registerRequest, String selectedTaskName) {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK_REGISTER)
                .newBuilder()
                .addQueryParameter(registerRequest, selectedTaskName)
                .addQueryParameter("username", this.username)
                .build()
                .toString();

        HttpClientUtil.runAsyncWithEmptyBody(finalUrl, "POST", new Callback() {

            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        ShowPopUp(Alert.AlertType.ERROR, "Error", null, e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                String message = response.header("message");

                if (response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(()-> ShowPopUp(Alert.AlertType.INFORMATION, "Registration Successfully!", null, message));
                else //Failed
                    Platform.runLater(()-> ShowPopUp(Alert.AlertType.ERROR, "Registration Failed!", null, message));

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    //------------------------------------------- Task Information  ---------------------------------------------//
    @FXML void TaskSelectedFromTaskListView() {
        String selectedTaskName = this.TasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        this.RegisterToTaskButton.setDisable(false);
        this.chosenTask = selectedTaskName;

        sendTaskUpdateRequestToServer();
    }

    private void sendTaskUpdateRequestToServer() {
        String finalUrl = HttpUrl
                .parse(Patterns.TASK)
                .newBuilder()
                .addQueryParameter("task-info", this.chosenTask)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for task-info!"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    String body = Objects.requireNonNull(response.body()).string();
                    Platform.runLater(() ->
                    {
                        AllTaskDetails taskDetails = new Gson().fromJson(body, AllTaskDetails.class);
                        refreshWorkerTaskDetails(taskDetails);
                    });
                } else //Failed
                    Platform.runLater(() -> System.out.println("couldn't pull graph-dto from server!"));

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    public void refreshWorkerTaskDetails(AllTaskDetails taskDetails) {
        WorkerDashboardController.this.TaskNameTextField.setText(taskDetails.getTaskName());
        WorkerDashboardController.this.CreatedByTextField.setText(taskDetails.getUploader());

        updateTaskTargetDetailsTable(taskDetails);
        updateTaskStatusTable(taskDetails);
    }

    private void updateTaskTargetDetailsTable(AllTaskDetails taskDetailsDTO) {

        SelectedGraphTableItem selectedGraphTableItem = new SelectedGraphTableItem(taskDetailsDTO.getRoots(),
                taskDetailsDTO.getMiddles(), taskDetailsDTO.getLeaves(), taskDetailsDTO.getIndependents());

        WorkerDashboardController.this.selectedTaskTargetsList.clear();
        WorkerDashboardController.this.selectedTaskTargetsList.add(selectedGraphTableItem);

        WorkerDashboardController.this.TaskTargetsTableView.setItems(WorkerDashboardController.this.selectedTaskTargetsList);
    }

    private void updateTaskStatusTable(AllTaskDetails taskDetailsDTO) {

        WorkerTaskStatusTableItem taskStatusTableItem = new WorkerTaskStatusTableItem(taskDetailsDTO.getTaskType(), taskDetailsDTO.getTaskStatus(),
                taskDetailsDTO.getRegisteredWorkers(), taskDetailsDTO.getTotalPayment(), WorkerDashboardController.this.username);

        WorkerDashboardController.this.selectedTaskStatusList.clear();
        WorkerDashboardController.this.selectedTaskStatusList.add(taskStatusTableItem);

        WorkerDashboardController.this.TaskStatusTableView.setItems(WorkerDashboardController.this.selectedTaskStatusList);

        if(taskDetailsDTO.getRegisteredWorkers().contains(WorkerDashboardController.this.username))
        {
            WorkerDashboardController.this.RegisterToTaskButton.setText("Unregister From Task");
            WorkerDashboardController.this.register = false;
        }
        else
        {
            WorkerDashboardController.this.RegisterToTaskButton.setText("Register To Task");
            WorkerDashboardController.this.register = true;
        }
    }
}