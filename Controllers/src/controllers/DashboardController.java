package controllers;

import com.google.gson.Gson;
import http.HttpClientUtil;
import javafx.application.Platform;
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

public class DashboardController {

    @FXML private TitledPane OnlineGraphsTiltedPane;
    @FXML private ListView<String> OnlineGraphsListView;
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

    public void setPrimaryController(PrimaryController primaryController) {
        this.primaryController = primaryController;
    }

    public void setUsername(String username) {
        this.username = username;
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
                    Platform.runLater(() -> {


                    });
                }
            }
        });
    }

    public void newGraphUploaded() {
        LoadGraphButtonPressed();
    }
}
