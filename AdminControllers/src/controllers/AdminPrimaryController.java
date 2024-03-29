package controllers;

import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import paths.BodyComponentsPaths;
import patterns.Patterns;
import resources.checker.ResourceChecker;
import summaries.GraphSummary;
import target.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class AdminPrimaryController {
    //--------------------------------------------------Members-----------------------------------------------------//
    private Stage primaryStage;
    private static Graph graph = null;
    private AdminDashboardController dashboardController;
    private AdminGraphDetailsController adminGraphDetailsController;
    private AdminTaskControlController adminTaskControlController;
    private AdminCreateTaskController adminCreateTaskController;
    private AdminConnectionsController adminConnectionsController;
    private SplitPane DashboardPane = null;
    private ScrollPane graphDetailsPane = null;
    private ScrollPane connectionsPane = null;
    private ScrollPane createTaskPane = null;
    private ScrollPane taskControlPane = null;
    private String userName;

    //---------------------------------------------- FXML Members --------------------------------------------------//
    @FXML private BorderPane mainBorderPane;
    @FXML private Button graphDetailsButton;
    @FXML private Button connectionsButton;
    @FXML private Button CreateTaskButton;
    @FXML private Button TaskControlButton;
    @FXML private Button ChatButton;
    @FXML private RadioMenuItem defaultTheme;
    @FXML private RadioMenuItem darkModeTheme;

    //------------------------------------------------- Settings ----------------------------------------------------//
    public void initialize(Stage primaryStage, String userName) {
        setUserName(userName);
        setPrimaryStage(primaryStage);
        UpdateDashboardControllerAndPane();

        this.DashboardPane.getStylesheets().clear();
        this.DashboardPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    //------------------------------------------------- Toolbar ----------------------------------------------------//
    @FXML void loadXMLButtonPressed(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);

        if(selectedFile != null)
            uploadFileToServer(Patterns.GRAPH, selectedFile);
    }

    public void uploadFileToServer(String url, File file) {

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("fileToUpload", file.getName(),
                        RequestBody.create(file, MediaType.parse("xml")))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body).addHeader("username", this.userName)
                .addHeader("upload-graph", "upload-graph")
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(()-> ShowPopUp(Alert.AlertType.ERROR, "Error in loading file!", null, e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(response.code() >= 200 && response.code() < 300)
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.INFORMATION, "File loaded successfully!", null, response.header("message")));
                else
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Error in loading file!", null, response.header("message")));
            }
        });
    }

    public void loadGraph(File file) {
        if(file == null)
            return;

        if(!OverrideGraph())
            return;

        try{
            ResourceChecker rc = new ResourceChecker();

            //Loading the graph from the xml file
            this.graph = rc.extractFromXMLToGraph(file.toPath());
            GraphSummary graphSummary = new GraphSummary(graph);

            //Updating the panes and controllers for the loaded graph
            updatePanesAndControllers();

            setGraphOnControllers();
            //Setting Graphviz and display its outcome

            RefreshCurrentCenterPane();

            //Knowing the user the file loaded successfully
            ShowPopUp(Alert.AlertType.INFORMATION, "File loaded Successfully", null, "The graph " + this.graph.getGraphName() + " loaded successfully!");
        }
        catch(Exception ex)
        {
            ShowPopUp(Alert.AlertType.ERROR, "Error", null, ex.getMessage());
        }
    }

    private void setGraphOnControllers() throws FileNotFoundException {
        this.adminGraphDetailsController.setGraph(this.graph);
        this.adminConnectionsController.setGraph(this.graph);
    }

    private void RefreshCurrentCenterPane() {
        graphDetailsButtonPressed(new ActionEvent());
    }

    //------------------------------------------------- Themes ----------------------------------------------------//
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

    private void updateThemeOnAllPanes(String themePath) {
        if(this.graph != null) {
            this.graphDetailsPane.getStylesheets().clear();
            this.graphDetailsPane.getStylesheets().add(themePath);
            this.connectionsPane.getStylesheets().clear();
            this.connectionsPane.getStylesheets().add(themePath);
            this.createTaskPane.getStylesheets().clear();
            this.createTaskPane.getStylesheets().add(themePath);
        }

        updateThemeOnTaskControlPane(themePath);
    }

    private void updateThemeOnTaskControlPane(String themePath) {
        if(this.taskControlPane != null)
        {
            this.taskControlPane.getStylesheets().clear();
            this.taskControlPane.getStylesheets().add(themePath);
        }
    }

    private void UpdatePanesStyles() {
        if(this.defaultTheme.isSelected())
            defaultThemePressed(new ActionEvent());
        else if(this.darkModeTheme.isSelected())
            darkModeThemePressed(new ActionEvent());
        else //this.rainbowModeTheme.isSelected()
            rainbowThemePressed(new ActionEvent());
    }

    //------------------------------------------------- Sidebar ----------------------------------------------------//
    private void EnableSidebarButtons() {
        this.graphDetailsButton.setDisable(false);
        this.connectionsButton.setDisable(false);
        this.CreateTaskButton.setDisable(false);
    }

    @FXML void DashboardButtonPressed(ActionEvent event) {

        if(this.DashboardPane == null)
            UpdateDashboardControllerAndPane();

        this.mainBorderPane.setCenter(this.DashboardPane);
    }

    public void TaskPulledFromServer(String taskName, String graphName) {
        UpdateTaskControlControllerAndPane(taskName);
        UpdatePanesStyles();

        this.TaskControlButton.setDisable(false);
        TaskControlButtonPressed(new ActionEvent());
        this.adminTaskControlController.setTaskStaticInformation(taskName, graphName);
    }

    @FXML void connectionsButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.connectionsPane);
    }

    @FXML void graphDetailsButtonPressed(ActionEvent event)
    {
        this.mainBorderPane.setCenter(this.graphDetailsPane);
    }

    @FXML void CreateTaskButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.createTaskPane);
    }

    @FXML void TaskControlButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.taskControlPane);
    }

    //------------------------------------------------ Load Panes --------------------------------------------------//
    private void updatePanesAndControllers() {
        UpdateGraphDetailsControllerAndPane();
        UpdateConnectionsControllerAndPane();
        UpdateCreateTaskControllerAndPane();
        EnableSidebarButtons();
        UpdatePanesStyles();
    }

    private void UpdateConnectionsControllerAndPane() {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.CONNECTIONS);
        loader.setLocation(url);
        try {
            this.connectionsPane = loader.load(url.openStream());
            this.adminConnectionsController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateGraphDetailsControllerAndPane() {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.GRAPH_DETAILS);
        loader.setLocation(url);
        try {
            this.graphDetailsPane = loader.load(url.openStream());
            this.adminGraphDetailsController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateTaskControlControllerAndPane(String taskName) {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.TASK_CONTROL);
        loader.setLocation(url);
        try {
            this.taskControlPane = loader.load(url.openStream());
            this.adminTaskControlController = loader.getController();
            this.adminTaskControlController.initialize(taskName, this.userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateCreateTaskControllerAndPane() {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.CREATE_TASK);
        loader.setLocation(url);
        try {
            this.createTaskPane = loader.load(url.openStream());
            this.adminCreateTaskController = loader.getController();
            this.adminCreateTaskController.initialize(this.userName, this.graph);
            this.adminCreateTaskController.setUserName(this.userName);
            this.adminCreateTaskController.setGraph(this.graph);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateDashboardControllerAndPane() {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.DASHBOARD);
        loader.setLocation(url);
        try {
            this.DashboardPane = loader.load(url.openStream());
            this.dashboardController = loader.getController();
            this.dashboardController.initialize(this, this.userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------- General ----------------------------------------------------//
    private Boolean OverrideGraph() {
        if(this.graph == null)
            return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Override existed graph");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to override the graph " + this.graph.getGraphName() + "?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton );
        Optional<ButtonType> result = alert.showAndWait();

        return result.get() == yesButton;
    }

    private void ShowPopUp(Alert.AlertType alertType, String title, String header, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //------------------------------------------------ Not Used ----------------------------------------------------//
    private void FileLoadedSuccessfully() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("File loaded Successfully");
        alert.setHeaderText(null);
        alert.setContentText("The graph " + this.graph.getGraphName() + " loaded successfully!");
        alert.showAndWait();
    }
}