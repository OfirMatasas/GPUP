package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class ShowDetailsController {

    @FXML
    private AnchorPane AnchorPane;

    @FXML
    private ChoiceBox<?> TargetSerialSetChoiceBox;

    @FXML
    private ListView<?> SerialSetsListsView;

    @FXML
    private TableView<?> TargetsDetailsTable;

    @FXML
    private TableColumn<?, ?> TargetNumber;

    @FXML
    private TableColumn<?, ?> TargetName;

    @FXML
    private TableColumn<?, ?> TargetProperty;

    @FXML
    private TableColumn<?, ?> TargetDirectDependsOn;

    @FXML
    private TableColumn<?, ?> TargetAllDependsOn;

    @FXML
    private TableColumn<?, ?> TargetDirectRequiredFor;

    @FXML
    private TableColumn<?, ?> TargetAllRequiredFor;

    @FXML
    private TableColumn<?, ?> TargetSerialSets;

    @FXML
    private TableView<?> TargetPropertiesTable;

    @FXML
    private TableColumn<?, ?> RootsProperty;

    @FXML
    private TableColumn<?, ?> MiddlesProperty;

    @FXML
    private TableColumn<?, ?> LeavesProperty;

    @FXML
    private TableColumn<?, ?> IndependetsProperty;

    @FXML
    private Label SerialSetsLabel;

    @FXML
    private Label GraphProperties;

    @FXML
    private PieChart PropertiesPie;

}
