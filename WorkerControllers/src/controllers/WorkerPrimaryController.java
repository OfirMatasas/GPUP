package controllers;

import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import paths.BodyComponentsPaths;
import target.Graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class WorkerPrimaryController {
    //--------------------------------------------------Members-----------------------------------------------------//
    private Stage primaryStage;
    private static Graph graph = null;
    private WorkerDashboardController workerDashboardController = null;
    private WorkerTasksController workerTasksController = null;
    private SplitPane DashboardPane = null;
    private ScrollPane taskControlPane = null;
    private String userName;

    @FXML private ToggleGroup templates;
    @FXML private BorderPane mainBorderPane;
    @FXML private HBox HboxForLogo;
    @FXML private ImageView PrimaryLogo;
    @FXML private ScrollPane statusBar;
    @FXML private Button DashboardButton;
    @FXML private Button graphDetailsButton;
    @FXML private Button connectionsButton;
    @FXML private Button CreateTaskButton;
    @FXML private Button TaskControlButton;
    @FXML private Menu file;
    @FXML private MenuItem loadXMLButton;
    @FXML private MenuItem saveProgressButton;
    @FXML private MenuItem exitButton;
    @FXML private Menu themes;
    @FXML private RadioMenuItem defaultTheme;
    @FXML private RadioMenuItem darkModeTheme;
    @FXML private RadioMenuItem rainbowTheme;
    @FXML private Menu Help;
    @FXML private MenuItem about;
    @FXML private AnchorPane StatusBar;
    private SimpleStringProperty selectedFileProperty;
    private SimpleBooleanProperty isFileSelected;
    private FileWriter dotFile;

    //--------------------------------------------------Settings-----------------------------------------------------//
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void initialize(Stage primaryStage, String userName)
    {
        setUserName(userName);
        setPrimaryStage(primaryStage);
        UpdateDashboardControllerAndPane();

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
    }
    //--------------------------------------------------Toolbar-----------------------------------------------------//


    public void uploadTaskUpdateToServer(String url, File file) {

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("fileToUpload", file.getName(),
                        RequestBody.create(file, MediaType.parse("xml")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body).addHeader("username", this.userName)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("got graph response - failed");
                Platform.runLater(()-> ShowPopUp(Alert.AlertType.ERROR, "Error in loading file!", null, e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                System.out.println("got graph response - success");
                if(response.code() >= 200 && response.code() < 300)
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.INFORMATION, "File loaded successfully!", null, response.header("message")));
                else
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Error in loading file!", null, response.header("message")));
            }
        });
    }

    //--------------------------------------------------Themes-----------------------------------------------------//
    @FXML void defaultThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);

        updateThemeOnAllPanes(BodyComponentsPaths.LIGHT_CENTER_THEME);
    }

    @FXML void darkModeThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.DARK_MAIN_THEME)).toExternalForm());

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);

        updateThemeOnAllPanes(BodyComponentsPaths.DARK_CENTER_THEME);
    }

    @FXML void rainbowThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.RAINBOW_MAIN_THEME)).toExternalForm());

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);

        updateThemeOnAllPanes(BodyComponentsPaths.RAINBOW_CENTER_THEME);
    }

    private void updateThemeOnAllPanes(String themePath)
    {
        updateThemeOnTaskControlPane(themePath);
    }

    private void updateThemeOnTaskControlPane(String themePath) {
        if(this.taskControlPane != null)
        {
            this.taskControlPane.getStylesheets().clear();
            this.taskControlPane.getStylesheets().add(themePath);
        }
    }
    //--------------------------------------------------Sidebar-----------------------------------------------------//
    private void UpdateDashboardControllerAndPane()
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.DASHBOARD);
        loader.setLocation(url);
        try {
            this.DashboardPane = loader.load(url.openStream());
            this.workerDashboardController = loader.getController();
            this.workerDashboardController.initialize(this, this.userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateTaskControlControllerAndPane(String taskName)
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.TASK_CONTROL);
        loader.setLocation(url);
        try {
            this.taskControlPane = loader.load(url.openStream());
            this.workerTasksController = loader.getController();
            this.workerTasksController.initialize(taskName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void EnableSidebarButtons() {
        this.graphDetailsButton.setDisable(false);
        this.connectionsButton.setDisable(false);
        this.CreateTaskButton.setDisable(false);
    }

    @FXML void DashboardButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.DashboardPane);
    }

    public void TaskPulledFromServer(String taskName, String graphName)
    {
        UpdateTaskControlControllerAndPane(taskName);
        UpdatePanesStyles();

        this.TaskControlButton.setDisable(false);
        TaskControlButtonPressed(new ActionEvent());
        this.workerTasksController.setTaskStaticInformation(taskName, graphName);
    }

    @FXML void TaskControlButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.taskControlPane);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    public static void ShowPopUp(Alert.AlertType alertType, String title, String header, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void UpdatePanesStyles()
    {
        if(this.defaultTheme.isSelected())
            defaultThemePressed(new ActionEvent());
        else if(this.darkModeTheme.isSelected())
            darkModeThemePressed(new ActionEvent());
        else
            rainbowThemePressed(new ActionEvent());
    }
}