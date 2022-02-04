package controllers;

import com.google.gson.Gson;
import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import paths.Patterns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class DashboardController {

    @FXML private TitledPane OnlineGraphsTiltedPane;
    @FXML private ListView<String> OnlineGraphsListView;
    private final ObservableList<String> onlineGraphsList = FXCollections.observableArrayList();
    @FXML private Button AddNewGraphButton;
    @FXML private Button LoadGraphButton;
    @FXML private TitledPane OnlineAdminsTiltedPane;
    @FXML private ListView<?> onlineAdminsListView;
    @FXML private TitledPane OnlineWorkersTiltedPane;
    @FXML private ListView<?> onlineWorkersListView;
    @FXML private TitledPane OnlineTasksTiltedPane;
    @FXML private ListView<?> AllTasksListView;
    @FXML private ListView<?> myTasksListView;
    @FXML private Button ControlSelectedTaskButton;
    @FXML private Font x11;
    @FXML private Color x21;
    @FXML private TextField GraphNameTextField;
    @FXML private TextField uploadedByTextField;
    @FXML private TextField GraphNameTextField1;
    @FXML private TextField GraphNameTextField11;
    @FXML private TableView<?> GraphTargetsTableView;
    @FXML private TableColumn<?, ?> GraphTargetsAmount;
    @FXML private TableColumn<?, ?> GraphIndependentAmount;
    @FXML private TableColumn<?, ?> GraphLeafAmount;
    @FXML private TableColumn<?, ?> GraphMiddleAmount;
    @FXML private TableColumn<?, ?> GraphRootAmount;
    @FXML private Font x1;
    @FXML private Color x2;
    @FXML private TextField TaskNameTextField;
    @FXML private TextField CreatedByTextField;
    @FXML private TextField TaskOnGraphTextField;
    @FXML private TableView<?> typeTableView1;
    @FXML private TableColumn<?, ?> TaskTargetsAmount;
    @FXML private TableColumn<?, ?> TaskIndependentAmount;
    @FXML private TableColumn<?, ?> TaskLeafAmount;
    @FXML private TableColumn<?, ?> TaskMiddleAmount;
    @FXML private TableColumn<?, ?> TaskRootAmount;
    @FXML private TableView<?> typeTableView11;
    @FXML private TableColumn<?, ?> TaskStatus;
    @FXML private TableColumn<?, ?> currentWorkers;
    @FXML private TableColumn<?, ?> TaskWorkPayment;
    private PrimaryController primaryController;
    private String username;
    private PullerThread pullerThread;

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
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        );
                    } else //Failed
                    {
                        Platform.runLater(() -> System.out.println("couldn't pull graph-list from server!"));
                    }
                }
            });
        }

        private void refreshGraphList(Set<String> graphlist)
        {
            for(String curr : graphlist)
            {
                if(!DashboardController.this.onlineGraphsList.contains(curr))
                    DashboardController.this.onlineGraphsList.add(curr);
            }
        }

        private void refreshUsersLists() {

        }

        private void getAndUpdateUsersLists() {

        }
    }

    public void initialize(PrimaryController primaryController, String username)
    {
        setPrimaryController(primaryController);
        setUsername(username);
        createPullingThread();

        setupListeners();
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
    }

    private void createPullingThread() {
        this.pullerThread = new PullerThread();
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
//        String selectedGraphName = this.OnlineGraphsListView.getSelectionModel().getSelectedItem();

        String selectedGraphName = "Test";
//        if(selectedGraphName == null)
//            return;

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
                    System.out.println("Just got " +  graphFile.getName() + " file from server!");
                    Platform.runLater(()-> DashboardController.this.primaryController.loadGraph(graphFile));
                } else //Failed
                {
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Loading File Failure", null, response.message()));
                }
            }
        });
    }

    public void newGraphUploaded() {
        LoadGraphButtonPressed();
    }
}
