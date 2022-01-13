package controllers;

import graphAnalyzers.CircleFinder;
import graphAnalyzers.PathFinder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import target.Graph;
import target.Target;

import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionsController {
    //--------------------------------------------------Members-----------------------------------------------------//
    public ToggleGroup whatIfValue;
    private Graph graph = null;
    private final ObservableList<String> allTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> relationsList = FXCollections.observableArrayList();
    private final ObservableList<String> destinationTargets = FXCollections.observableArrayList();
    private String originTargetName;
    private String relation;

    @FXML private ScrollPane scrollPane;
    @FXML private Pane pane;
    @FXML private GridPane ConnectionsGridPane;
    @FXML private AnchorPane TargetsConnectionAnchorPane;
    @FXML private Label ShowConnectionLabel;
    @FXML private Label OriginTargetLabel;
    @FXML private ChoiceBox<String> OriginTargetChoiceBox;
    @FXML private Label RelationLabel;
    @FXML private ChoiceBox<String> RelationChoiceBox;
    @FXML private Label DestinationTargetLabel;
    @FXML private ChoiceBox<String> DestinationTargetChoiceBox;
    @FXML private ScrollPane ListViewScrollPane;
    @FXML private AnchorPane CheckCirclesAnchorPane;
    @FXML private ChoiceBox<String> CircleTargetChoiceBox;
    @FXML private Label CheckCirclesLabel;
    @FXML private Label ChooseCheckCircleLabel;
    @FXML private ListView<String> CirclesListView;
    @FXML private AnchorPane WhatIfAnchorPane;
    @FXML private ChoiceBox<String> WhatIfChoiceBox;
    @FXML private Label WhatIFLabel;
    @FXML private Label ChooseWhatIfTarget;
    @FXML private ListView<String> WhatIfListView;
    @FXML private RadioButton DependsOnRadioButton;
    @FXML private ListView<String> showConnectionBetweenListView;
    @FXML private ToggleGroup What_If_Value;
    @FXML private RadioButton RequiredForRadioButton;
    @FXML private Button calculatePathsButton;
    @FXML private AnchorPane connectionsAnchorPane;
    @FXML private AnchorPane AnchorPane;


    //--------------------------------------------------Settings-----------------------------------------------------//


    public void setGraph(Graph graph)
    {
        this.graph = graph;

        setAllTargetsList();
        setRelationChoiceBox();
        SetTooltips();
    }

    @FXML void calculatePathsPressed(ActionEvent event) {
        Target origin,destination;
        ArrayList<String> paths;
        ObservableList<String> pathsTargets = FXCollections.observableArrayList();
        PathFinder pathFinder = new PathFinder();
        String destinationTargetName = this.DestinationTargetChoiceBox.getValue();
        String originTargetName = this.OriginTargetChoiceBox.getValue();

        origin = this.graph.getTarget(this.OriginTargetChoiceBox.getValue());
        destination = this.graph.getTarget(destinationTargetName);

        if(this.RelationChoiceBox.getValue().equals("Depends on"))
            paths = pathFinder.getPathsFromTargets(origin,destination, Target.Connection.DEPENDS_ON);
        else
            paths = pathFinder.getPathsFromTargets(origin,destination, Target.Connection.REQUIRED_FOR);

        if(paths.isEmpty())
            pathsTargets.addAll("No paths between " + originTargetName + " and " + destinationTargetName);
        else
            pathsTargets.addAll(paths);

        this.showConnectionBetweenListView.setItems(pathsTargets);
    }

    private void setRelationChoiceBox()
    {
        this.relationsList.add(0, "Depends on");
        this.relationsList.add(1, "Required for");

        this.RelationChoiceBox.setItems(this.relationsList);
    }

    private void setAllTargetsList()
    {
        int i = 0;
        for(Target currentTargetName : this.graph.getGraphTargets().values())
            this.allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = this.allTargetsList.sorted();

        this.OriginTargetChoiceBox.setItems(sorted);
        this.DestinationTargetChoiceBox.setItems(sorted);
        this.CircleTargetChoiceBox.setItems(sorted);
        this.WhatIfChoiceBox.setItems(sorted);
    }

    private void SetTooltips()
    {
        this.OriginTargetChoiceBox.setTooltip(new Tooltip("Choose an origin target"));
        this.RelationChoiceBox.setTooltip(new Tooltip("Choose a relation between the targets"));
        this.DestinationTargetChoiceBox.setTooltip(new Tooltip("Choose a destination target"));

        this.CircleTargetChoiceBox.setTooltip(new Tooltip("Choose a target"));

        this.WhatIfChoiceBox.setTooltip(new Tooltip("Choose a target"));
    }

    //--------------------------------------------Targets Connection-----------------------------------------------//
    public void OriginTargetChosen(ActionEvent actionEvent) {
        this.calculatePathsButton.setDisable(this.RelationChoiceBox.getValue() == null || this.DestinationTargetChoiceBox.getValue() == null);
    }

    public void RelationChosen(ActionEvent actionEvent) {
        this.calculatePathsButton.setDisable(this.OriginTargetChoiceBox.getValue() == null || this.DestinationTargetChoiceBox.getValue() == null);
    }

    public void DestinationTargetChosen(ActionEvent actionEvent) {
        this.calculatePathsButton.setDisable(this.OriginTargetChoiceBox.getValue() == null || this.RelationChoiceBox.getValue() == null);
    }

    //--------------------------------------------------Circle-----------------------------------------------------//
    public void CheckIfTargetCircled(ActionEvent actionEvent) {
        CircleFinder circleFinder = new CircleFinder();
        circleFinder.checkIfCircled(this.graph.getTarget(this.CircleTargetChoiceBox.getValue()));

        String circlePath = circleFinder.getCirclePath();
        ObservableList<String> circleList = FXCollections.observableArrayList();

        if(circlePath == null)
            circleList.add("Not in a circle");
        else
            circleList.addAll(Arrays.asList(circlePath.split(" ")));

        this.CirclesListView.setItems(circleList);

    }

    public void WhatIfTargetSelected(ActionEvent actionEvent)
    {
        String selectedTarget = this.WhatIfChoiceBox.getValue();
        ObservableList<String> otherTargets = FXCollections.observableArrayList();

        if(this.DependsOnRadioButton.isSelected())
            otherTargets.addAll(this.graph.getTarget(selectedTarget).getAllDependsOnTargets());
        else if(this.RequiredForRadioButton.isSelected())
            otherTargets.addAll(this.graph.getTarget(selectedTarget).getAllRequiredForTargets());

        this.WhatIfListView.setItems(otherTargets);
    }

    //--------------------------------------------------What-if-----------------------------------------------------//
    public void WhatIfRadioButtonSelected(ActionEvent actionEvent) {
        if(this.WhatIfChoiceBox.getValue() != null)
            WhatIfTargetSelected(actionEvent);
    }
}
