package controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import resources.checker.ResourceChecker;
import target.Graph;

import java.io.File;

public class PrimaryController {

    private Stage primaryStage;
    private Boolean confirmExit;
    Graph graph;

    @FXML
    private BorderPane mainBorderPane;

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

    @FXML
    void aboutPressed(ActionEvent event)
    {

    }

    @FXML
    void connectionsButtonPressed(ActionEvent event) {

    }

    @FXML
    void darkModeThemePressed(ActionEvent event) {

    }

    @FXML
    void defaultThemePressed(ActionEvent event) {

    }

    @FXML
    void enableAnimationsPressed(ActionEvent event) {

    }

    @FXML
    void graphDetailsButtonPressed(ActionEvent event) {

    }

    @FXML
    void loadXMLButtonPressed(ActionEvent event)
    {
        Alert alert;
        ResourceChecker rc = new ResourceChecker();
        FileChooser fileChooser = new FileChooser();
        File selectedFile;
        fileChooser.setTitle("Select a file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));

        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if(selectedFile == null)
            return;

        try{
            graph = rc.extractFromXMLToGraph(selectedFile.toPath());
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File loaded Successfully");
            alert.setHeaderText(null);
            alert.setContentText("The graph " + graph.getGraphName() + " loaded successfully!");
            alert.showAndWait();
        }
        catch(Exception ex)
        {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error loading file");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void rainbowThemePressed(ActionEvent event) {

    }

    @FXML
    void saveProgressPressed(ActionEvent event) {

    }

    @FXML
    void taskButtonPressed(ActionEvent event) {

    }
}