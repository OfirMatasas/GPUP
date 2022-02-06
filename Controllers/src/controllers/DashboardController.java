package controllers;

import com.google.gson.Gson;
import dtos.DashboardGraphDetailsDTO;
import dtos.DashboardTaskDetailsDTO;
import http.HttpClientUtil;
import information.SelectedGraphTableItem;
import information.SelectedTaskStatusTableItem;
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
import paths.Patterns;
import task.CompilationTaskInformation;
import task.SimulationTaskInformation;
import users.UsersLists;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class DashboardController {

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
    private PrimaryController primaryController;
    private String username;
    private PullerThread pullerThread;
    private Thread usersListsRefreshThread;

    public void initialize(PrimaryController primaryController, String username)
    {
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
                .parse(Patterns.LOCAL_HOST + Patterns.GRAPHS)
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
                    Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        {
                                            DashboardGraphDetailsDTO graphDetailsDTO = gson.fromJson(responseBody.string(), DashboardGraphDetailsDTO.class);
                                            refreshGraphDetailsDTO(graphDetailsDTO);
                                            responseBody.close();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                } else //Failed
                    Platform.runLater(() -> System.out.println("couldn't pull graph-dto from server!"));
            }


            private void refreshGraphDetailsDTO(DashboardGraphDetailsDTO graphDetailsDTO) {
                DashboardController.this.GraphNameTextField.setText(graphDetailsDTO.getGraphName());
                DashboardController.this.uploadedByTextField.setText(graphDetailsDTO.getUploader());
                DashboardController.this.SimulationPriceTextField.setText(graphDetailsDTO.getSimulationPrice().toString());
                DashboardController.this.CompilationPriceTextField.setText(graphDetailsDTO.getCompilationPrice().toString());

                updateTargetDetailsTable(graphDetailsDTO);
            }

            private void updateTargetDetailsTable(DashboardGraphDetailsDTO graphDetailsDTO) {

                SelectedGraphTableItem selectedGraphTableItem = new SelectedGraphTableItem(graphDetailsDTO.getRoots(),
                        graphDetailsDTO.getMiddles(), graphDetailsDTO.getLeaves(), graphDetailsDTO.getIndependents());

                DashboardController.this.selectedGraphTargetsList.clear();
                DashboardController.this.selectedGraphTargetsList.add(selectedGraphTableItem);

                DashboardController.this.GraphTargetsTableView.setItems(DashboardController.this.selectedGraphTargetsList);
            }

        });
    }

    public void AddNewGraphButtonPressed(MouseEvent mouseEvent) throws IOException {
        this.primaryController.loadXMLButtonPressed(new ActionEvent());
    }

    public void TaskSelectedFromAllListView(MouseEvent mouseEvent) {
        String selectedTaskName = this.AllTasksListView.getSelectionModel().getSelectedItem();

        if(selectedTaskName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Patterns.LOCAL_HOST + Patterns.TASKS)
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
                    Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        {
                                            DashboardTaskDetailsDTO taskDetailsDTO = gson.fromJson(responseBody.string(), DashboardTaskDetailsDTO.class);
                                            refreshTaskDetailsDTO(taskDetailsDTO);
                                            responseBody.close();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
                } else //Failed
                    Platform.runLater(() -> System.out.println("couldn't pull graph-dto from server!"));
            }

            private void refreshTaskDetailsDTO(DashboardTaskDetailsDTO taskDetailsDTO) {
                DashboardController.this.TaskNameTextField.setText(taskDetailsDTO.getTaskName());
                DashboardController.this.CreatedByTextField.setText(taskDetailsDTO.getUploader());
                DashboardController.this.TaskOnGraphTextField.setText(taskDetailsDTO.getGraphName());

                updateTaskTargetDetailsTable(taskDetailsDTO);
                updateTaskStatusTable(taskDetailsDTO);
            }

            private void updateTaskTargetDetailsTable(DashboardTaskDetailsDTO taskDetailsDTO) {

                SelectedGraphTableItem selectedGraphTableItem = new SelectedGraphTableItem(taskDetailsDTO.getRoots(),
                        taskDetailsDTO.getMiddles(), taskDetailsDTO.getLeaves(), taskDetailsDTO.getIndependents());

                DashboardController.this.selectedTaskTargetsList.clear();
                DashboardController.this.selectedTaskTargetsList.add(selectedGraphTableItem);

                DashboardController.this.TaskTargetsTableView.setItems(DashboardController.this.selectedTaskTargetsList);
            }

            private void updateTaskStatusTable(DashboardTaskDetailsDTO taskDetailsDTO) {

                SelectedTaskStatusTableItem selectedTaskStatusTableItem = new SelectedTaskStatusTableItem(taskDetailsDTO.getTaskStatus(),
                        taskDetailsDTO.getTotalWorkers(), taskDetailsDTO.getTotalPayment());

                DashboardController.this.selectedTaskStatusList.clear();
                DashboardController.this.selectedTaskStatusList.add(selectedTaskStatusTableItem);

                DashboardController.this.TaskStatusTableView.setItems(DashboardController.this.selectedTaskStatusList);
            }
        });
    }

    public void TaskSelectedFromMyListView(MouseEvent mouseEvent) {
        this.ControlSelectedTaskButton.setDisable(false);
    }

    public class PullerThread extends Thread
    {
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
            }
        }

        private void refreshGraphList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.LOCAL_HOST + Patterns.GRAPH_LIST)
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
                        Platform.runLater(() ->
                            {
                                Gson gson = new Gson();
                                ResponseBody responseBody = response.body();
                                try {
                                    if (responseBody != null) {
                                        Set graphList = gson.fromJson(responseBody.string(), Set.class);
                                        refreshGraphList(graphList);
                                        responseBody.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        );
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull graph-list from server!"));
                }
            });
        }

        private void refreshGraphList(Set<String> graphList)
        {
            if(graphList == null)
                return;

            for(String curr : graphList)
            {
                if(!DashboardController.this.onlineGraphsList.contains(curr))
                    DashboardController.this.onlineGraphsList.add(curr);
            }
        }

        private void refreshAllTasksList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.LOCAL_HOST + Patterns.TASK_LIST)
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
                        Platform.runLater(() ->
                                {
                                    Gson gson = new Gson();
                                    ResponseBody responseBody = response.body();
                                    try {
                                        if (responseBody != null) {
                                            Set taskList = gson.fromJson(responseBody.string(), Set.class);
                                            refreshAllTasksList(taskList);
                                            responseBody.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull all-task-list from server!"));
                }
            });
        }

        private void refreshAllTasksList(Set<String> taskList)
        {
            if(taskList == null)
                return;

            for(String curr : taskList)
            {
                if(!DashboardController.this.onlineTasksList.contains(curr))
                    DashboardController.this.onlineTasksList.add(curr);
            }
        }

        private void refreshMyTasksList() {
            String finalUrl = HttpUrl
                    .parse(Patterns.LOCAL_HOST + Patterns.TASK_LIST)
                    .newBuilder()
                    .addQueryParameter("my-tasks-list", "my-tasks-list")
                    .addQueryParameter("username", DashboardController.this.username)
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
                        Platform.runLater(() ->
                                {
                                    Gson gson = new Gson();
                                    ResponseBody responseBody = response.body();
                                    try {
                                        if (responseBody != null) {
                                            Set taskList = gson.fromJson(responseBody.string(), Set.class);
                                            refreshMyTasksList(taskList);
                                            responseBody.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                    } else //Failed
                        Platform.runLater(() -> System.out.println("couldn't pull my-task-list from server!"));
                }
            });
        }

        private void refreshMyTasksList(Set<String> taskList)
        {
            if(taskList == null)
                return;

            for(String curr : taskList)
            {
                if(!DashboardController.this.myTasksList.contains(curr))
                    DashboardController.this.myTasksList.add(curr);
            }
        }

        private void refreshUsersLists() {
            String finalUrl = HttpUrl
                    .parse(Patterns.LOCAL_HOST + Patterns.USERS_LISTS)
                    .newBuilder()
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> System.out.println("Failure on connecting to server for users list!"));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Gson gson = new Gson();
                    ResponseBody responseBody = response.body();
                    UsersLists usersLists = gson.fromJson(responseBody.string(), UsersLists.class);
                    responseBody.close();

                    Platform.runLater(() -> updateUsersLists(usersLists));
                }
            });
        }
    }

    private void setupListeners() {
        this.onlineGraphsList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!DashboardController.this.OnlineGraphsListView.getItems().contains(curr))
                        DashboardController.this.OnlineGraphsListView.getItems().add(curr);
                }
            }
        });

        this.onlineTasksList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!DashboardController.this.AllTasksListView.getItems().contains(curr))
                        DashboardController.this.AllTasksListView.getItems().add(curr);
                }
            }
        });

        this.myTasksList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                for(String curr : c.getList())
                {
                    if(!DashboardController.this.myTasksListView.getItems().contains(curr))
                        DashboardController.this.myTasksListView.getItems().add(curr);
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

    public void setPrimaryController(PrimaryController primaryController) {
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
                .parse(Patterns.LOCAL_HOST + Patterns.TASKS)
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
                    Gson gson = new Gson();
                    ResponseBody responseBody = response.body();
                    if(response.header("task-type").equals("simulation"))
                    {
                        SimulationTaskInformation info = gson.fromJson(responseBody.string(), SimulationTaskInformation.class);
                        System.out.println("Just got " +  info.getTaskName() + " task from server!");

                        Platform.runLater(()-> DashboardController.this.primaryController.TaskPulledFromServer
                                (info.getTaskName(), info.getGraphName()));
                    }
                    else
                    {
                        CompilationTaskInformation info = gson.fromJson(responseBody.string(), CompilationTaskInformation.class);
                        System.out.println("Just got " +  info.getTaskName() + " task from server!");

                        Platform.runLater(()-> DashboardController.this.primaryController.TaskPulledFromServer
                                (info.getTaskName(), info.getGraphName()));
                    }
                    responseBody.close();
                } else //Failed
                {
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Loading File Failure", null, response.header("error")));
                }
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
                .parse(Patterns.LOCAL_HOST + Patterns.GRAPHS)
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
                    Gson gson = new Gson();
                    ResponseBody responseBody = response.body();
                    File graphFile = gson.fromJson(responseBody.string(), File.class);
                    responseBody.close();
                    System.out.println("Just got " +  graphFile.getName() + " file from server!");
                    Platform.runLater(()-> DashboardController.this.primaryController.loadGraph(graphFile));
                } else //Failed
                {
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Loading File Failure", null, response.header("error")));
                }
            }
        });
    }

    public void newGraphUploaded() {
        LoadGraphButtonPressed();
    }
}
