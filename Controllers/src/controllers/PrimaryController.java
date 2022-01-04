package controllers;

import bodyComponentsPaths.BodyComponentsPaths;
import com.sun.webkit.ColorChooser;
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
import resources.checker.ResourceChecker;
import summaries.GraphSummary;
import target.Graph;
import target.Target;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    private ArrayList<String> colors;
    private File selectedFile;

    @FXML
    private ToggleGroup templates;

    @FXML
    private BorderPane mainBorderPane;

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
    void enableAnimationsPressed(ActionEvent event) {

    }

    @FXML
    void loadXMLButtonPressed(ActionEvent event)
    {
        ResourceChecker rc = new ResourceChecker();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if(selectedFile == null)
            return;

        if(!OverrideGraph())
            return;

        try{
            graph = rc.extractFromXMLToGraph(selectedFile.toPath());
            parallelThreads = rc.getParallelThreads();
            updatePanesAndControllers();
            graphDetailsController.setGraph(graph);
            connectionsController.setGraph(graph);


            taskController.setGraph(graph,selectedFile.getName());
            graphSummary = new GraphSummary(graph, rc.getWorkingDirectoryPath());
            convertXMLToDot();
            RefreshCurrentCenterPane();
            FileLoadedSuccessfully();
        }
        catch(Exception ex)
        {
            ErrorPopup(ex, "Error loading file");
        }
    }

    public void setColorsForNodes()
    {
        colors = new ArrayList<>();
        colors.add("aqua");
        colors.add("aquamarine");
        colors.add("blueviolet");
        colors.add("brown1");
        colors.add("crimson");
        colors.add("darkorchid");
        colors.add("deeppink");
        colors.add("forestgreen");
        colors.add("goldenrod1");
        colors.add("gray46");
        colors.add("greenyellow");
        colors.add("hotpink");
        colors.add("orangered");
        colors.add("seagreen1");
        colors.add("steelblue1");
        colors.add("royalblue2");
        colors.add("teal");
        colors.add("olive");
        colors.add("lightcoral");
    }
    public void convertXMLToDot() throws IOException {

        setColorsForNodes();
        Random rnd = new Random();
        int randomColor = rnd.nextInt(colors.size());

        String directoryPath = graphSummary.getWorkingDirectory();
        String fileNameDOT = selectedFile.getName().substring(0,selectedFile.getName().lastIndexOf('.')) + ".dot";
        String fileNamePNG = selectedFile.getName().substring(0,selectedFile.getName().lastIndexOf('.')) + ".png";

        String properties = "digraph G {\n" + "node [margin=0 fontcolor=black fontsize=28 width=1 shape=circle style=filled fillcolor= " + colors.get(randomColor) +"]\n" +
                "\n" +
                "nodesep = 2;\n" +
                "ranksep = 2;\n";

        try {
            FileWriter dotFile = new FileWriter(new File(directoryPath,fileNameDOT));
            dotFile.write(properties);

            for (Target target : graph.getGraphTargets().values()) {
                dotFile.write(target.getTargetName());
                if (!target.getDependsOnTargets().isEmpty())
                    dotFile.write("-> {" + printAllDependsOnTarget(target) + "}\n");

                dotFile.write("\n");
            }
        dotFile.write("}");
        dotFile.close();

        String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
        Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd \\ && cd " + directoryPath + " && " + createPNGFromDOT + " && exit");




        }
        catch(IOException e) {
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


    private void RefreshCurrentCenterPane() throws Exception {
//        if(mainBorderPane.getCenter() == graphDetailsPane)
            graphDetailsButtonPressed(new ActionEvent());
//        else if(mainBorderPane.getCenter() == connectionsPane)
//            connectionsButtonPressed(new ActionEvent());
//        else if(mainBorderPane.getCenter() == taskPane)
//            taskButtonPressed(new ActionEvent());
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


        if(graph == null)
            return;

        graphDetailsPane.getStylesheets().clear();
        graphDetailsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
        connectionsPane.getStylesheets().clear();
        connectionsPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
        taskPane.getStylesheets().clear();
        taskPane.getStylesheets().add(BodyComponentsPaths.LIGHT_CENTER_THEME);
    }

    @FXML
    void darkModeThemePressed(ActionEvent event) {
        Scene scene = primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.DARK_MAIN_THEME)).toExternalForm());

        if(graph == null)
            return;

        graphDetailsPane.getStylesheets().clear();
        graphDetailsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
        connectionsPane.getStylesheets().clear();
        connectionsPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
        taskPane.getStylesheets().clear();
        taskPane.getStylesheets().add(BodyComponentsPaths.DARK_CENTER_THEME);
    }

    @FXML
    void rainbowThemePressed(ActionEvent event) {
        Scene scene = primaryStage.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.RAINBOW_MAIN_THEME)).toExternalForm());

        if(graph == null)
            return;

        graphDetailsPane.getStylesheets().clear();
        graphDetailsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
        connectionsPane.getStylesheets().clear();
        connectionsPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
        taskPane.getStylesheets().clear();
        taskPane.getStylesheets().add(BodyComponentsPaths.RAINBOW_CENTER_THEME);
    }
    //--------------------------------------------------Sidebar-----------------------------------------------------//
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

    private void UpdateButtons() {
        graphDetailsButton.setDisable(false);
        connectionsButton.setDisable(false);
        taskButton.setDisable(false);
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