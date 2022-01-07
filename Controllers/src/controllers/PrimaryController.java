package controllers;

import bodyComponentsPaths.BodyComponentsPaths;
import javafx.animation.*;
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
import resources.checker.ResourceChecker;
import summaries.GraphSummary;
import target.Graph;
import target.Target;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class PrimaryController {
    //--------------------------------------------------Members-----------------------------------------------------//
    private Stage primaryStage;
    private Graph graph = null;
    private GraphDetailsController graphDetailsController;
    private TaskController taskController;
    private ConnectionsController connectionsController;
    private ScrollPane graphDetailsPane;
    private ScrollPane connectionsPane = null;
    private ScrollPane taskPane = null;
    private int parallelThreads;
    private GraphSummary graphSummary;
    private final ArrayList<String> rootColors = new ArrayList<>();
    private final ArrayList<String> middleColors = new ArrayList<>();
    private final ArrayList<String>leafColors = new ArrayList<>();
    private final ArrayList<String>independentColors = new ArrayList<>();
    private File selectedFile;

    @FXML
    private ToggleGroup templates;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ImageView fireWorksImageView;

    @FXML
    private HBox HboxForLogo;

    @FXML
    private ImageView PrimaryLogo;

    @FXML
    private ScrollPane statusBar;

    @FXML
    private Button graphDetailsButton;

    @FXML
    private Button connectionsButton;

    @FXML
    private Button taskButton;

    @FXML
    private Menu file;

    @FXML
    private MenuItem loadXMLButton;

    @FXML
    private MenuItem saveProgressButton;

    @FXML
    private MenuItem exitButton;

    @FXML
    private Menu animations;

    @FXML
    private CheckBox enableAnimations;

    @FXML
    private Menu themes;

    @FXML
    private RadioMenuItem defaultTheme;

    @FXML
    private RadioMenuItem darkModeTheme;

    @FXML
    private RadioMenuItem rainbowTheme;

    @FXML
    private Menu Help;

    @FXML
    private MenuItem about;

    @FXML
    private AnchorPane StatusBar;
    private SimpleStringProperty selectedFileProperty;
    private SimpleBooleanProperty isFileSelected;

    //--------------------------------------------------Toolbar-----------------------------------------------------//
    @FXML
    void aboutPressed(ActionEvent event)
    {

    }

   @FXML
   void enableAnimationsPressed(ActionEvent event)
   {
       FadeTransition fadeTransition = new FadeTransition();
       ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), this.fireWorksImageView);

       fadeTransition.setNode(PrimaryLogo);
       fadeTransition.setDuration(Duration.millis(2000));
       fadeTransition.setCycleCount(TranslateTransition.INDEFINITE);
       fadeTransition.setInterpolator(Interpolator.LINEAR);
       fadeTransition.setFromValue(1);
       fadeTransition.setToValue(0);


       this.fireWorksImageView.setVisible(true);
       scaleTransition.setCycleCount(100);
       scaleTransition.setToX(-1);
       scaleTransition.setToY(-1);

       fadeTransition.play();
       scaleTransition.play();
   }


    @FXML
    void loadXMLButtonPressed(ActionEvent event)
    {
        ResourceChecker rc = new ResourceChecker();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        String selectedFileComposedName;
        String directoryPath;
        if(selectedFile == null)
            return;

        if(!OverrideGraph())
            return;

        try{
            //Loading the graph from the xml file
            graph = rc.extractFromXMLToGraph(selectedFile.toPath());
            parallelThreads = rc.getParallelThreads();

            //Updating the panes and controllers for the loaded graph
            updatePanesAndControllers();
            setGraphOnControllers();
            graphSummary = new GraphSummary(graph, rc.getWorkingDirectoryPath());

            //Setting Graphviz and display its outcome
            convertXMLToDot();
            RefreshCurrentCenterPane();

            //Knowing the user the file loaded successfully
            FileLoadedSuccessfully();
        }
        catch(Exception ex)
        {
            ErrorPopup(ex, "Error loading file");
        }
    }

    private void setGraphOnControllers() {
        this.graphDetailsController.setGraph(this.graph);
        this.connectionsController.setGraph(this.graph);
        this.taskController.setGraph(this.graph);
    }

    public void setColorsForNodes()
    {
        rootColors.add("aqua");
        rootColors.add("aquamarine");
        rootColors.add("blueviolet");
        rootColors.add("brown1");
        rootColors.add("teal");
        middleColors.add("crimson");
        middleColors.add("darkorchid");
        middleColors.add("deeppink");
        middleColors.add("forestgreen");
        middleColors.add("olive");
        leafColors.add("goldenrod1");
        leafColors.add("gray46");
        leafColors.add("greenyellow");
        leafColors.add("hotpink");
        leafColors.add("lightcoral");
        independentColors.add("orangered");
        independentColors.add("seagreen1");
        independentColors.add("steelblue1");
        independentColors.add("royalblue2");
    }
    public void convertXMLToDot() {

        setColorsForNodes();
//        Random rnd = new Random();
//        int rootRandomColor = rnd.nextInt(rootColors.size());
//        int middleRandomColor = rnd.nextInt(middleColors.size());
//        int leafRandomColor = rnd.nextInt(leafColors.size());
//        int independentColor = rnd.nextInt(independentColors.size());
        String currentColor;
        Target.TargetPosition targetPosition;
        String directoryPath = graphSummary.getWorkingDirectory();
        String fileNameDOT = "GeneratedGraph.dot";
        String fileNamePNG = "GeneratedGraph.png";
        String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
        String properties = "digraph G {\n" + "node [margin=0 fontcolor=black fontsize=28 width=1 shape=circle style=filled]\n" +
                "\n" +
                "nodesep = 2;\n" +
                "ranksep = 2;\n";

        try {
            FileWriter dotFile = new FileWriter(new File(directoryPath,fileNameDOT));
            dotFile.write(properties);

            for (Target target : graph.getGraphTargets().values())
            {
                dotFile.write(target.getTargetName());

                targetPosition = target.getTargetPosition();
                if(targetPosition.equals(Target.TargetPosition.ROOT))
                    currentColor = "dodgerblue";
                else if(targetPosition.equals(Target.TargetPosition.MIDDLE))
                    currentColor = "Gold";
                else if(targetPosition.equals(Target.TargetPosition.LEAF))
                    currentColor = "green3";
                else
                    currentColor = "chocolate1";

                dotFile.write(" [fillcolor = " +  currentColor + "]\n");
            }

            for (Target target : graph.getGraphTargets().values()) {
                dotFile.write(target.getTargetName());

                if (!target.getDependsOnTargets().isEmpty())
                    dotFile.write("-> {" + printAllDependsOnTarget(target) + "}");
                dotFile.write("\n");

//                dotFile.write("\n");
            }
        dotFile.write("}");
        dotFile.close();

        Process process = Runtime.getRuntime().exec("cmd /c cmd.exe /K \"cd \\ && cd " + directoryPath + " && " + createPNGFromDOT + " && exit");
        process.waitFor();
        taskController.setGraphImage(directoryPath + "\\" + fileNamePNG);
        }
        catch(IOException | InterruptedException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }
    }

    private String printAllDependsOnTarget(Target curTarget)
    {
        String DependedTarget = "";
        for (Target dependsOnTarget : curTarget.getDependsOnTargets())
        {
            DependedTarget = DependedTarget + dependsOnTarget.getTargetName() + " ";
        }
        return DependedTarget;
    }

    private void RefreshCurrentCenterPane() {
        graphDetailsButtonPressed(new ActionEvent());
    }

    @FXML
    void saveProgressPressed(ActionEvent event) {

    }
    //--------------------------------------------------Themes-----------------------------------------------------//
    @FXML
    void defaultThemePressed(ActionEvent event) {
        Scene scene = primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());

        if(graph != null)
        {
            graphDetailsPane.getStylesheets().clear();
            graphDetailsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
            connectionsPane.getStylesheets().clear();
            connectionsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
            taskPane.getStylesheets().clear();
            taskPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
        }
    }

    @FXML
    void darkModeThemePressed(ActionEvent event) {
        Scene scene = primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.DARK_MAIN_THEME)).toExternalForm());

        if(graph != null)
        {
            graphDetailsPane.getStylesheets().clear();
            graphDetailsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
            connectionsPane.getStylesheets().clear();
            connectionsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
            taskPane.getStylesheets().clear();
            taskPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
        }
    }

    @FXML
    void rainbowThemePressed(ActionEvent event) {
        Scene scene = primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.RAINBOW_MAIN_THEME)).toExternalForm());

        if(graph != null)
        {
            graphDetailsPane.getStylesheets().clear();
            graphDetailsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
            connectionsPane.getStylesheets().clear();
            connectionsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
            taskPane.getStylesheets().clear();
            taskPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
        }
    }
    //--------------------------------------------------Sidebar-----------------------------------------------------//
    private void UpdateButtons() {
        graphDetailsButton.setDisable(false);
        connectionsButton.setDisable(false);
        taskButton.setDisable(false);
    }

    @FXML
    void connectionsButtonPressed(ActionEvent event) {
        mainBorderPane.setCenter(connectionsPane);
    }

    @FXML
    void graphDetailsButtonPressed(ActionEvent event)
    {
        mainBorderPane.setCenter(graphDetailsPane);
    }

    @FXML
    void taskButtonPressed(ActionEvent event) {
        mainBorderPane.setCenter(taskPane);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void setPrimaryStage(Stage stage){
        this.primaryStage = stage;
    }

    private Boolean OverrideGraph()
    {
        if(graph == null)
            return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Override existed graph");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to override the graph " + graph.getGraphName() + "?");
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
        alert.setContentText("The graph " + graph.getGraphName() + " loaded successfully!");
        alert.showAndWait();


    }

    private void ErrorPopup(Exception ex, String title)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage());
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
            connectionsPane = loader.load(url.openStream());
            connectionsController = loader.getController();
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
            graphDetailsPane = loader.load(url.openStream());
            graphDetailsController = loader.getController();
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
            taskPane = loader.load(url.openStream());
            taskController = loader.getController();
            taskController.setParallelThreads(parallelThreads);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdatePanesStyles()
    {
        if(defaultTheme.isSelected())
            defaultThemePressed(new ActionEvent());
        else if(darkModeTheme.isSelected())
            darkModeThemePressed(new ActionEvent());
        else
            rainbowThemePressed(new ActionEvent());
    }
}