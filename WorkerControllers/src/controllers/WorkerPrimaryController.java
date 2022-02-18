package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import paths.BodyComponentsPaths;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class WorkerPrimaryController {
    //------------------------------------------------- Members ----------------------------------------------------//
    private Stage primaryStage;
    private WorkerDashboardController workerDashboardController = null;
    private WorkerTasksController workerTasksController = null;
    private SplitPane DashboardPane = null;
    private SplitPane TasksPane = null;
    private String userName;
    private Integer numOfThreads;

    //----------------------------------------------- FXML Members -------------------------------------------------//
    @FXML private BorderPane mainBorderPane;

    //------------------------------------------------- Settings ---------------------------------------------------//
    public void initialize(Stage primaryStage, String userName, Integer numOfThreads) {
        setUserName(userName);
        setNumOfThreads(numOfThreads);
        setPrimaryStage(primaryStage);

        UpdateDashboardControllerAndPane();
        UpdateTasksControllerAndPane();

        defaultThemePressed(new ActionEvent());
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    private void setNumOfThreads(Integer numOfThreads) { this.numOfThreads = numOfThreads; }

    private void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    //------------------------------------------------- Themes ----------------------------------------------------//
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

    private void updateThemeOnAllPanes(String themePath) {
        this.TasksPane.getStylesheets().clear();
        this.TasksPane.getStylesheets().add(themePath);

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(themePath);
    }

    //------------------------------------------------- Sidebar ----------------------------------------------------//
    private void UpdateDashboardControllerAndPane() {
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

    private void UpdateTasksControllerAndPane() {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.TASKS);
        loader.setLocation(url);
        try {
            this.TasksPane = loader.load(url.openStream());
            this.workerTasksController = loader.getController();
            this.workerTasksController.initialize(this.userName, this.numOfThreads);
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

    //------------------------------------------------- General ----------------------------------------------------//
    public static void ShowPopUp(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}