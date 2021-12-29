package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import target.Graph;

public class TaskController {

    private Graph graph;

    @FXML
    private BorderPane taskBorderPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private HBox toolBarHbox;

    @FXML
    private Button runButton;

    @FXML
    private Button PauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button deselectAllButton;

    @FXML
    private Pane leftPane;

    @FXML
    private ComboBox<?> taskSelection;

    @FXML
    private ComboBox<?> targetSelection;

    @FXML
    private ComboBox<?> affectedTargets;

    @FXML
    private ListView<?> currentSelectedTargetListView;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tableViewTabPane;

    @FXML
    private TableView<?> taskTargetDetailsTableView;

    @FXML
    private TableColumn<?, ?> numberColumn;

    @FXML
    private TableColumn<?, ?> targetNameColumn;

    @FXML
    private TableColumn<?, ?> positionColumn;

    @FXML
    private TableColumn<?, ?> directDependsOnColumn;

    @FXML
    private TableColumn<?, ?> directRequiredForColumn;

    @FXML
    private TableColumn<?, ?> executionTimeStatus;

    @FXML
    private TableColumn<?, ?> finalStatus;

    @FXML
    private Tab graphViewTabPane;

    @FXML
    private Tab graphViewTabPane1;

    @FXML
    private Pane footerPane;

    @FXML
    private Label proccesingTimeLabel;

    @FXML
    private Label limitedPermanentLabel;

    @FXML
    private Label successRateLabel;

    @FXML
    private Label successRateWithWarnings;

    @FXML
    private Spinner<?> processingTimeSpinner;

    @FXML
    private RadioButton limitedRadioButton;

    @FXML
    private ToggleGroup limitedOrPermanent;

    @FXML
    private RadioButton permanentRadioButton;

    @FXML
    private Slider successRateSlider;

    @FXML
    private Slider successRatewithWarningsSlider;

    @FXML
    private Label successRateValueLabel;

    @FXML
    private Label successRateWithWarningsValueLabel;

    @FXML
    void affectedTargetsPressed(ActionEvent event) {

    }

    @FXML
    void deselectAllPressed(ActionEvent event) {

    }

    @FXML
    void graphViewTabPressed(ActionEvent event) {

        this.footerPane.setVisible(false);

    }

    @FXML
    void limitedOptionPressed(ActionEvent event) {

    }

    @FXML
    void logViewTabPressed(ActionEvent event) {

    }

    @FXML
    void pausePressed(ActionEvent event) {

    }

    @FXML
    void permanentOptionPressed(ActionEvent event) {

    }

    @FXML
    void runPressed(ActionEvent event) {

    }

    @FXML
    void selectAllPressed(ActionEvent event) {

    }

    @FXML
    void stopPressed(ActionEvent event) {

    }

    @FXML
    void tableViewTabPressed(ActionEvent event) {

    }

    @FXML
    void targetSelectionPressed(ActionEvent event) {

    }

    @FXML
    void taskSelectionPressed(ActionEvent event) {

    }

    public void setGraph(Graph graph) {

        this.graph=graph;
    }
}
