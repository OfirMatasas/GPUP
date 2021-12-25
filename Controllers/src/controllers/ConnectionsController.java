package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class ConnectionsController {

    @FXML
    private GridPane ConnectionsGridPane;

    @FXML
    private AnchorPane TargetsConnectionAnchorPane;

    @FXML
    private Label ShowConnectionLabel;

    @FXML
    private Label OriginTargetLabel;

    @FXML
    private Label DestinationTargetLabel;

    @FXML
    private ChoiceBox<?> OriginTargetChoiceBox;

    @FXML
    private ChoiceBox<?> DestinationTargetChoiceBox;

    @FXML
    private ScrollPane ListViewScrollPane;

    @FXML
    private ListView<?> ConnectionsListView;

    @FXML
    private AnchorPane CheckCirclesAnchorPane;

    @FXML
    private ChoiceBox<?> CircleTargetChoiceBox;

    @FXML
    private Label CheckCirclesLabel;

    @FXML
    private Label ChooseCheckCircleLabel;

    @FXML
    private ListView<?> CirclesListView;

    @FXML
    private AnchorPane WhatIfAnchorPane;

    @FXML
    private ChoiceBox<?> WhatIfChoiceBox;

    @FXML
    private Label WhatIFLabel;

    @FXML
    private Label ChooseWhatIfTarget;

    @FXML
    private ListView<?> WhatIfListView;

    @FXML
    private RadioButton DependsOnRadioButton;

    @FXML
    private ToggleGroup What_If_Value;

    @FXML
    private RadioButton RequiredForRadioButton;

}
