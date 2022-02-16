package controllers;

import com.google.gson.Gson;
import dtos.DashboardGraphDetailsDTO;
import information.AllTaskDetails;
import http.HttpClientUtil;
import tableItems.SelectedGraphTableItem;
import tableItems.SelectedTaskStatusTableItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;
import users.UsersLists;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class AdminDashboardController {

    private final ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private final ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    @FXML private TitledPane OnlineGraphsTiltedPane;
    @FXML private ListView<String> OnlineGraphsListView;
    private final ObservableList<String> onlineGraphsList = FXCollections.observableArrayList();
    private final ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private final ObservableList<String> myTasksList = FXCollections.observableArrayList();
    private final ObservableList<SelectedGraphTableItem> selectedGraphTargetsList = FXCollections.observableArrayList();
    private final ObservableList<SelectedGraphTableItem> selectedTaskTargetsList = FXCollections.observableArrayList();
    private final ObservableList<SelectedTaskStatusTableItem> selectedTaskStatusList = FXCollections.observableArrayList();
    @FXML private Button AddNewGraphButton;
    @FXML private Button LoadGraphButton;
    @FXML private TitledPane OnlineAdminsTiltedPane;
    @FXML private ListView<String> onlineAdminsListView;
    @FXML private TitledPane OnlineWorkersTiltedPane;
    @FXML private ListView<String> onlineWorkersListView;
    @FXML private TitledPane OnlineTasksTiltedPane;
    @FXML private ListView<String> AllTasksListView;
    @FXML private ListView<String> myTasksListView;
    @FXML private Button ControlSelectedTaskButton;
    @FXML private Font x11;
    @FXML private Color x21;
    @FXML private TextField GraphNameTextField;
    @FXML private TextField uploadedByTextField;
    @FXML private TextField SimulationPriceTextField;
    @FXML private TextField CompilationPriceTextField;
    @FXML private TableView<SelectedGraphTableItem> GraphTargetsTableView;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> GraphTargetsAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> GraphIndependentAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> GraphLeafAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> GraphMiddleAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> GraphRootAmount;
    @FXML private Font x1;
    @FXML private Color x2;
    @FXML private TextField TaskNameTextField;
    @FXML private TextField CreatedByTextField;
    @FXML private TextField TaskOnGraphTextField;
    @FXML private TableView<SelectedGraphTableItem> TaskTargetsTableView;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> TaskTargetsAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> TaskIndependentAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> TaskLeafAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> TaskMiddleAmount;
    @FXML private TableColumn<SelectedGraphTableItem, Integer> TaskRootAmount;
    @FXML private TableView<SelectedTaskStatusTableItem> TaskStatusTableView;
    @FXML private TableColumn<SelectedTaskStatusTableItem, String> TaskStatus;
    @FXML private TableColumn<SelectedTaskStatusTableItem, Integer> currentWorkers;
    @FXML private TableColumn<SelectedTaskStatusTableItem, Integer> TaskWorkPayment;
    private AdminPrimaryController primaryController;
    private String username;
    private PullerThread pullerThread;
    private String chosenTask = null;

    public void initialize(AdminPrimaryController primaryController, String username) {
        setPrimaryController(primaryController);
        setUsername(username);
        createPullingThread();
        setupListeners();
        initializeGraphTargetDetailsTable();
        initializeTaskTargetDetailsTable();
        initializeTaskStatusTable();
    }

    private void updateUsersLists(UsersLists usersLists) {
        this.onlineAdminsList.clear();
        this.onlineWorkersList.clear();
        this.onlineAdminsList.addAll(usersLists.getAdminsList());
        this.onlineWorkersList.addAll(usersLists.getWorkersList());
    }

    public void initializeGraphTargetDetailsTable() {
        this.GraphTargetsAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("targets"));
        this.GraphRootAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("roots"));
        this.GraphMiddleAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("middles"));
        this.GraphLeafAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("leaves"));
        this.GraphIndependentAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("independents"));
    }

    public void initializeTaskTargetDetailsTable() {
        this.TaskTargetsAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("targets"));
        this.TaskRootAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("roots"));
        this.TaskMiddleAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("middles"));
        this.TaskLeafAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("leaves"));
        this.TaskIndependentAmount.setCellValueFactory(new PropertyValueFactory<SelectedGraphTableItem, Integer>("independents"));
    }

    public void initializeTaskStatusTable() {
        this.TaskStatus.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, String>("status"));
        this.currentWorkers.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("workers"));
        this.TaskWorkPayment.setCellValueFactory(new PropertyValueFactory<SelectedTaskStatusTableItem, Integer>("totalPayment"));
    }

    public void GraphSelectedFromListView(MouseEvent mouseEvent) {
        String selectedGraphName = this.OnlineGraphsListView.getSelectionModel().getSelectedItem();

        if(selectedGraphName == null)
            return;

        this.LoadGraphButton.setDisable(false);

        String finalUrl = HttpUrl
                .parse(Patterns.GRAPH)
                .newBuilder()
                .addQueryParameter("graph-details-DTO", selectedGraphName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failure on connecting to server for graph-dto!"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    String body = Objects.requireNonNull(response.body()).string();
                    Platform.runLater(() ->
                    {
                        DashboardGraphDetailsDTO graphDetailsDTO = new Gson().fromJson(body, DashboardGraphDetailsDTO.class);
                        refreshGraphDetailsDTO(graphDetailsDTO);
                    });
                } else //Failed
                    Platform.runLater(() -> System.out.println("couldn't pull graph-dto from server!"));

                Objects.requireNonNull(response.body()).close();
            }

            private void refreshGraphDetailsDTO(DashboardGraphDetailsDTO graphDetailsDTO) {
                AdminDashboardController.this.GraphNameTextField.setText(graphDetailsDTO.getGraphName());
                AdminDashboardController.this.uploadedByTextField.setText(graphDetailsDTO.getUploader());
                AdminDashboardController.this.SimulationPriceTextField.setText(graphDetailsDTO.getSimulationPrice().toString());
                AdminDashboardController.this.CompilationPriceTextField.setText(graphDetailsDTO.getCompilationPrice().toString());

                updateTargetDetailsTable(graphDetailsDTO);
            }

            private void updateTargetDetailsTable(DashboardGraphDetailsDTO graphDetailsDTO) {

                SelectedGraphTableItem selectedGraphTableItem = new SelectedGraphTableItem(graphDetailsDTO.getRoots(),
                        graphDetailsDTO.getMiddles(), graphDetailsDTO.getLeaves(), graphDetailsDTO.getIndependents());

                AdminDashboardController.this.selectedGraphTargetsList.clear();
                AdminDashboardController.this.selectedGraphTargetsList.add(selectedGraphTableItem);

                AdminDashboardController.this.GraphTargetsTableView.setItems(AdminDashboardController.this.selectedGraphTargetsList);
            }

        });
    }

    public void AddNewGraphButtonPressed() throws IOException {
        this.primaryController.loadXMLButtonPressed(new ActionEvent());
    }

    public void TaskSelectedFromAllListView() {
        String selectedTaskName = this.AllTasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        this.chosenTask = selectedTaskName;

        String finalUrl = HttpUrl
                .parse(Patterns.TASK)
                .newBuilder()
                .addQueryParameter("task-info", selectedTaskName)
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
                        AllTaskDetails taskDetailsDTO = new Gson().fromJson(body, AllTaskDetails.class);
                        refreshTaskDetailsDTO(taskDetailsDTO);
                    });
                } else //Failed
                    Platform.runLater(() -> System.out.println("couldn't pull task-info from server!"));

                Objects.requireNonNull(response.body()).close();
            }

            private void refreshTaskDetailsDTO(AllTaskDetails taskDetailsDTO) {
                AdminDashboardController.this.TaskNameTextField.setText(taskDetailsDTO.getTaskName());
                AdminDashboardController.this.CreatedByTextField.setText(taskDetailsDTO.getUploader());
                AdminDashboardController.this.TaskOnGraphTextField.setText(taskDetailsDTO.getGraphName());

                updateTaskTargetDetailsTable(taskDetailsDTO);
                updateTaskStatusTable(taskDetailsDTO);
            }

            private void updateTaskTargetDetailsTable(AllTaskDetails taskDetailsDTO) {

                SelectedGraphTableItem selectedGraphTableItem = new SelectedGraphTableItem(taskDetailsDTO.getRoots(),
                        taskDetailsDTO.getMiddles(), taskDetailsDTO.getLeaves(), taskDetailsDTO.getIndependents());

                AdminDashboardController.this.selectedTaskTargetsList.clear();
                AdminDashboardController.this.selectedTaskTargetsList.add(selectedGraphTableItem);

                AdminDashboardController.this.TaskTargetsTableView.setItems(AdminDashboardController.this.selectedTaskTargetsList);
            }

            private void updateTaskStatusTable(AllTaskDetails taskDetailsDTO) {

                SelectedTaskStatusTableItem selectedTaskStatusTableItem = new SelectedTaskStatusTableItem(taskDetailsDTO.getTaskStatus(),
                        taskDetailsDTO.getRegisteredWorkers().size(), taskDetailsDTO.getTotalPayment());

                AdminDashboardController.this.selectedTaskStatusList.clear();
                AdminDashboardController.this.selectedTaskStatusList.add(selectedTaskStatusTableItem);

                AdminDashboardController.this.TaskStatusTableView.setItems(AdminDashboardController.this.selectedTaskStatusList);
            }
        });
    }

    public void TaskSelectedFromMyListView(MouseEvent mouseEvent) {
        this.ControlSelectedTaskButton.setDisable(false);
    }

    public class PullerThread extends Thread {
        @Override
        public void run()
        {
            while(true)
            {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshUsersLists();
                refreshGraphList();
                refreshAllTasksList();
                refreshMyTasksList();

                if(AdminDashboardController.this.chosenTask != null)
                    TaskSelectedFromAllListView();
            }
        }

        private void refreshGraphList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.GRAPH_LIST)
                    .newBuilder()
                    .addQueryParameter("graph-list", "graph-list")
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for graph-list!"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set graphList = new Gson().fromJson(body, Set.class);
                            refreshGraphList(graphList);
                        }
                        );
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull graph-list from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshGraphList(Set<String> graphList)
        {
            if(graphList == null)
                return;

            for(String curr : graphList)
            {
                if(!AdminDashboardController.this.onlineGraphsList.contains(curr))
                    AdminDashboardController.this.onlineGraphsList.add(curr);
            }
        }

        private void refreshAllTasksList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_LIST)
                    .newBuilder()
                    .addQueryParameter("all-tasks-list", "all-tasks-list")
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for task-list!"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set taskList = new Gson().fromJson(body, Set.class);
                            refreshAllTasksList(taskList);
                        });
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull all-task-list from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshAllTasksList(Set<String> taskList)
        {
            if(taskList == null)
                return;

            for(String curr : taskList)
            {
                if(!AdminDashboardController.this.onlineTasksList.contains(curr))
                    AdminDashboardController.this.onlineTasksList.add(curr);
            }
        }

        private void refreshMyTasksList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.TASK_LIST)
                    .newBuilder()
                    .addQueryParameter("my-tasks-list", "my-tasks-list")
                    .addQueryParameter("username", AdminDashboardController.this.username)
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for my-task-list!"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() >= 200 && response.code() < 300) //Success
                    {
                        String body = Objects.requireNonNull(response.body()).string();
                        Platform.runLater(() ->
                        {
                            Set taskList = new Gson().fromJson(body, Set.class);
                            refreshMyTasksList(taskList);
                        });
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull my-task-list from server!"));

                    Objects.requireNonNull(response.body()).close();
                }
            });
        }

        private void refreshMyTasksList(Set<String> taskList)
        {
            if(taskList == null)
                return;

            for(String curr : taskList)
            {
                if(!AdminDashboardController.this.myTasksList.contains(curr))
                    AdminDashboardController.this.myTasksList.add(curr);
            }
        }

        private void refreshUsersLists() {
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
    }

    private void setupListeners() {
        this.onlineGraphsList.addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!AdminDashboardController.this.OnlineGraphsListView.getItems().contains(curr))
                        AdminDashboardController.this.OnlineGraphsListView.getItems().add(curr);
                }
            }
        });

        this.onlineTasksList.addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!AdminDashboardController.this.AllTasksListView.getItems().contains(curr))
                        AdminDashboardController.this.AllTasksListView.getItems().add(curr);
                }
            }
        });

        this.myTasksList.addListener(new ListChangeListener<String>() {
            @Override public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!AdminDashboardController.this.myTasksListView.getItems().contains(curr))
                        AdminDashboardController.this.myTasksListView.getItems().add(curr);
                }
            }
        });
    }

    private void createPullingThread() {
        this.pullerThread = new PullerThread();

        this.onlineAdminsListView.setItems(this.onlineAdminsList);
        this.onlineWorkersListView.setItems(this.onlineWorkersList);

        this.pullerThread.setDaemon(true);
        this.pullerThread.start();

    }

    public void setPrimaryController(AdminPrimaryController primaryController) {
        this.primaryController = primaryController;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML void ControlSelectedTaskButtonClicked(ActionEvent event) {
        String selectedTaskName = this.myTasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Patterns.TASK)
                .newBuilder()
                .addQueryParameter("task", selectedTaskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        ShowPopUp(Alert.AlertType.ERROR, "Error", null, e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    String body = Objects.requireNonNull(response.body()).string();

                    if(Objects.equals(response.header("task-type"), "simulation"))
                    {
                        SimulationTaskInformation info = new Gson().fromJson(body, SimulationTaskInformation.class);
                        System.out.println("Just got " +  info.getTaskName() + " task from server!");

                        Platform.runLater(()-> AdminDashboardController.this.primaryController.TaskPulledFromServer
                                (info.getTaskName(), info.getGraphName()));
                    }
                    else
                    {
                        CompilationTaskInformation info = new Gson().fromJson(body, CompilationTaskInformation.class);
                        System.out.println("Just got " +  info.getTaskName() + " task from server!");

                        Platform.runLater(()-> AdminDashboardController.this.primaryController.TaskPulledFromServer
                                (info.getTaskName(), info.getGraphName()));
                    }
                }
                else
                {
                    String message = response.header("message");
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Loading File Failure", null, message));
                }

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private File convertResponseBodyToTempFile(Response response) {
        try {
            File tempFile = File.createTempFile("graph", ".xml");

            FileWriter fileWriter = new FileWriter(tempFile, true);
            fileWriter.write(Objects.requireNonNull(response.body()).string());

            return tempFile;

        } catch (IOException e) {
            ShowPopUp(Alert.AlertType.ERROR,"loading failed", null, "Error in loading graph file");
        }

        return null;
    }

    private void ShowPopUp(Alert.AlertType alertType, String title, String header, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void LoadGraphButtonPressed() {
        String selectedGraphName = this.OnlineGraphsListView.getSelectionModel().getSelectedItem();

        if(selectedGraphName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Patterns.GRAPH)
                .newBuilder()
                .addQueryParameter("graph", selectedGraphName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        ShowPopUp(Alert.AlertType.ERROR, "Error", null, e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    String body = Objects.requireNonNull(response.body()).string();
                    File graphFile = new Gson().fromJson(body, File.class);
                    System.out.println("Just got " +  graphFile.getName() + " file from server!");
                    Platform.runLater(()-> AdminDashboardController.this.primaryController.loadGraph(graphFile));
                } else //Failed
                {
                    String message = response.header("message");
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Loading File Failure", null, message));
                }

                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    public void newGraphUploaded() {
        LoadGraphButtonPressed();
    }
}
