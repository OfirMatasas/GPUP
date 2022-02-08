package controllers;

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
import paths.BodyComponentsPaths;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class WorkerPrimaryController {
    //--------------------------------------------------Members-----------------------------------------------------//
    private Stage primaryStage;
    private WorkerDashboardController workerDashboardController = null;
    private WorkerTasksController workerTasksController = null;
    private SplitPane DashboardPane = null;
    private SplitPane TasksPane = null;
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

    //--------------------------------------------------Settings-----------------------------------------------------//
    public void initialize(Stage primaryStage, String userName)
    {
        setUserName(userName);
        setPrimaryStage(primaryStage);

        UpdateDashboardControllerAndPane();
        UpdateTasksControllerAndPane();

        defaultThemePressed(new ActionEvent());
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    private void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    //--------------------------------------------------Themes-----------------------------------------------------//
    @FXML void defaultThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());

        updateThemeOnAllPanes(BodyComponentsPaths.LIGHT_CENTER_THEME);
    }

    @FXML void darkModeThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.DARK_MAIN_THEME)).toExternalForm());

        updateThemeOnAllPanes(BodyComponentsPaths.DARK_CENTER_THEME);
    }

    @FXML void rainbowThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.RAINBOW_MAIN_THEME)).toExternalForm());

        updateThemeOnAllPanes(BodyComponentsPaths.RAINBOW_CENTER_THEME);
    }

    private void updateThemeOnAllPanes(String themePath)
    {
        this.TasksPane.getStylesheets().clear();
        this.TasksPane.getStylesheets().add(themePath);

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(themePath);
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

    private void UpdateTasksControllerAndPane()
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.TASKS);
        loader.setLocation(url);
        try {
            this.TasksPane = loader.load(url.openStream());
            this.workerTasksController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML void DashboardButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.DashboardPane);
    }

    @FXML void TasksButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.TasksPane);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public static void ShowPopUp(Alert.AlertType alertType, String title, String header, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}