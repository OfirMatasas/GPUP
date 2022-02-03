package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class DashboardController {

    @FXML private TitledPane OnlineGraphsTiltedPane;
    @FXML private ListView<?> OnlineGraphsListView;
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


    @FXML void ControlSelectedTaskButtonClicked(ActionEvent event) {

    }

    private File convertResponseBodyToTempFile(Response response) {
        try {
            File tempFile = File.createTempFile("graph", ".xml");

            FileWriter fileWriter = new FileWriter(tempFile, true);
            fileWriter.write(Objects.requireNonNull(response.body()).string());

            return tempFile;

        } catch (IOException e) {
            ShowPopUp("Error in loading graph file", "loading failed", Alert.AlertType.ERROR);
        }

        return null;
    }

    private void ShowPopUp(String message, String title, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void AddNewGraphButtonClicked(MouseEvent mouseEvent) throws IOException {

    }
}
