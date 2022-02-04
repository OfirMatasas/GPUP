package controllers;

import http.HttpClientUtil;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import paths.BodyComponentsPaths;
import paths.Patterns;
import resources.checker.ResourceChecker;
import summaries.GraphSummary;
import target.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class PrimaryController {
    //--------------------------------------------------Members-----------------------------------------------------//
    private Stage primaryStage;
    private static Graph graph = null;
    private DashboardController dashboardController;
    private GraphDetailsController graphDetailsController;
    private TaskController taskController;
    private ConnectionsController connectionsController;
    private SplitPane DashboardPane = null;
    private ScrollPane graphDetailsPane = null;
    private ScrollPane connectionsPane = null;
    private ScrollPane taskPane = null;
    private int maxParallelThreads;
    private GraphSummary graphSummary;
    private FadeTransition fadeTransition;
    private ScaleTransition scaleTransition;
    private String userName;

    @FXML private ToggleGroup templates;
    @FXML private BorderPane mainBorderPane;
    @FXML private ImageView fireWorksImageView;
    @FXML private HBox HboxForLogo;
    @FXML private ImageView PrimaryLogo;
    @FXML private ScrollPane statusBar;
    @FXML private Button DashboardButton;
    @FXML private Button graphDetailsButton;
    @FXML private Button connectionsButton;
    @FXML private Button taskButton;
    @FXML private Menu file;
    @FXML private MenuItem loadXMLButton;
    @FXML private MenuItem saveProgressButton;
    @FXML private MenuItem exitButton;
    @FXML private Menu animations;
    @FXML private CheckBox enableAnimations;
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
    }
    //--------------------------------------------------Toolbar-----------------------------------------------------//
    @FXML void aboutPressed(ActionEvent event) {}

    @FXML void enableAnimationsPressed(ActionEvent event) {
        if (!this.enableAnimations.isSelected()) {
            this.fadeTransition.stop();
            this.scaleTransition.stop();
            this.fireWorksImageView.setVisible(false);

            this.fadeTransition.setDuration(Duration.millis(0));
            this.fadeTransition.setCycleCount(1);
            this.fadeTransition.setAutoReverse(false);
            this.fadeTransition.setInterpolator(Interpolator.LINEAR);

            this.fadeTransition.play();
            this.scaleTransition.play();

            return;
        }

        this.fadeTransition = new FadeTransition();
        this.scaleTransition = new ScaleTransition(Duration.seconds(1), this.fireWorksImageView);

        this.fadeTransition.setNode(this.PrimaryLogo);
        this.fadeTransition.setDuration(Duration.millis(2000));
        this.fadeTransition.setCycleCount(Animation.INDEFINITE);
        this.fadeTransition.setAutoReverse(true);
        this.fadeTransition.setInterpolator(Interpolator.LINEAR);
        this.fadeTransition.setFromValue(0);
        this.fadeTransition.setToValue(1);

        this.fireWorksImageView.setVisible(true);
        this.scaleTransition.setCycleCount(100);
        this.scaleTransition.setToX(-1);
        this.scaleTransition.setToY(-1);

        this.fadeTransition.play();
        this.scaleTransition.play();
    }

    @FXML void loadXMLButtonPressed(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);

        if(selectedFile != null)
            uploadFileToServer(Patterns.LOCAL_HOST + Patterns.GRAPHS, selectedFile);
//        loadGraph(selectedFile);
    }

    public void uploadFileToServer(String url, File file) {

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("fileToUpload", file.getName(),
                        RequestBody.create(file, MediaType.parse("xml")))
                .build();

        Request request = new Request.Builder()
                .url(Patterns.LOCAL_HOST + Patterns.GRAPHS)
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

    public void loadGraph(File file)
    {
        if(file == null)
            return;

        if(!OverrideGraph())
            return;

        try{
            ResourceChecker rc = new ResourceChecker();

            //Loading the graph from the xml file
            this.graph = rc.extractFromXMLToGraph(file.toPath());
            this.maxParallelThreads = rc.getParallelThreads();

            //Updating the panes and controllers for the loaded graph
            updatePanesAndControllers();

            this.graphSummary = new GraphSummary(this.graph, rc.getWorkingDirectoryPath());
            setGraphOnControllers();
            //Setting Graphviz and display its outcome

            RefreshCurrentCenterPane();

            //Knowing the user the file loaded successfully
            FileLoadedSuccessfully();
        }
        catch(Exception ex)
        {
            ShowPopUp(Alert.AlertType.ERROR, "Error", null, ex.getMessage());
        }
    }

    private void setGraphOnControllers() throws FileNotFoundException {
        this.graphDetailsController.setGraph(this.graph,this.graphSummary);
        this.connectionsController.setGraph(this.graph);
        this.taskController.setGraph(this.graph, this.graphSummary);
    }

    private void RefreshCurrentCenterPane() {
        graphDetailsButtonPressed(new ActionEvent());
    }

    @FXML void saveProgressPressed(ActionEvent event) { }

    //--------------------------------------------------Themes-----------------------------------------------------//
    @FXML void defaultThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());

        if(this.graph != null)
        {
            this.graphDetailsPane.getStylesheets().clear();
            this.graphDetailsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
            this.connectionsPane.getStylesheets().clear();
            this.connectionsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
            this.taskPane.getStylesheets().clear();
            this.taskPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
        }
    }

    @FXML void darkModeThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.DARK_MAIN_THEME)).toExternalForm());

        if(this.graph != null)
        {
            this.graphDetailsPane.getStylesheets().clear();
            this.graphDetailsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
            this.connectionsPane.getStylesheets().clear();
            this.connectionsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
            this.taskPane.getStylesheets().clear();
            this.taskPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
        }
    }

    @FXML void rainbowThemePressed(ActionEvent event) {
        Scene scene = this.primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.RAINBOW_MAIN_THEME)).toExternalForm());

        if(this.graph != null)
        {
            this.graphDetailsPane.getStylesheets().clear();
            this.graphDetailsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
            this.connectionsPane.getStylesheets().clear();
            this.connectionsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
            this.taskPane.getStylesheets().clear();
            this.taskPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
        }
    }
    //--------------------------------------------------Sidebar-----------------------------------------------------//
    private void UpdateButtons() {
        this.graphDetailsButton.setDisable(false);
        this.connectionsButton.setDisable(false);
        this.taskButton.setDisable(false);
    }

    @FXML void DashboardButtonPressed(ActionEvent event) {

        if(this.DashboardPane == null)
            UpdateDashboardControllerAndPane();

        this.mainBorderPane.setCenter(this.DashboardPane);
    }

    @FXML void connectionsButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.connectionsPane);
    }

    @FXML void graphDetailsButtonPressed(ActionEvent event)
    {
        this.mainBorderPane.setCenter(this.graphDetailsPane);
    }

    @FXML void taskButtonPressed(ActionEvent event) {
        this.mainBorderPane.setCenter(this.taskPane);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    private Boolean OverrideGraph()
    {
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

    private void FileLoadedSuccessfully()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("File loaded Successfully");
        alert.setHeaderText(null);
        alert.setContentText("The graph " + this.graph.getGraphName() + " loaded successfully!");
        alert.showAndWait();
    }

    private void ShowPopUp(Alert.AlertType alertType, String title, String header, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updatePanesAndControllers() {
        UpdateGraphDetailsControllerAndPane();
        UpdateConnectionsControllerAndPane();
        UpdateTaskControllerAndPane();
        UpdateButtons();
        UpdatePanesStyles();
    }

    private void UpdateConnectionsControllerAndPane()
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.CONNECTIONS);
        loader.setLocation(url);
        try {
            this.connectionsPane = loader.load(url.openStream());
            this.connectionsController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateGraphDetailsControllerAndPane()
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.GRAPH_DETAILS);
        loader.setLocation(url);
        try {
            this.graphDetailsPane = loader.load(url.openStream());
            this.graphDetailsController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateTaskControllerAndPane()
    {
        FXMLLoader loader = new FXMLLoader();
        URL url = getClass().getResource(BodyComponentsPaths.TASK);
        loader.setLocation(url);
        try {
            this.taskPane = loader.load(url.openStream());
            this.taskController = loader.getController();
            this.taskController.setMaxParallelThreads(this.maxParallelThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateDashboardControllerAndPane()
    {
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