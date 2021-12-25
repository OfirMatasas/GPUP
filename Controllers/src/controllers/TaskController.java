package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;

public class TaskController {

    @FXML
    private GridPane TaskGridPane;

    @FXML
    private GridPane TaskToolbarGridPane;

    @FXML
    private ToolBar TaskLeftToolbar;

    @FXML
    private Button RunButton;

    @FXML
    private Button PauseButton;

    @FXML
    private Button StopButton;

    @FXML
    private ToolBar RightToolbar;

    @FXML
    private Button SelectAllButton;

    @FXML
    private Button DeselectAllButton;

}
