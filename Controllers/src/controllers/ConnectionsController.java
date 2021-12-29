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
import java.util.Objects;
import java.util.Set;

public class ConnectionsController {
    //--------------------------------------------------Members-----------------------------------------------------//
    public ToggleGroup whatIfValue;
    private Graph graph = null;
    private final ObservableList<String> allTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> relationsList = FXCollections.observableArrayList();
    private final ObservableList<String> destinationTargets = FXCollections.observableArrayList();
    private String originTargetName;
    private String relation;
    private String destinationTargetName;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Pane pane;

    @FXML
    private GridPane ConnectionsGridPane;

    @FXML
    private AnchorPane TargetsConnectionAnchorPane;

    @FXML
    private Label ShowConnectionLabel;

    @FXML
    private Label OriginTargetLabel;

    @FXML
    private ChoiceBox<String> OriginTargetChoiceBox;

    @FXML
    private Label RelationLabel;

    @FXML
    private ChoiceBox<String> RelationChoiceBox;

    @FXML
    private Label DestinationTargetLabel;

    @FXML
    private ChoiceBox<String> DestinationTargetChoiceBox;

    @FXML
    private ScrollPane ListViewScrollPane;

    @FXML
    private AnchorPane CheckCirclesAnchorPane;

    @FXML
    private ChoiceBox<String> CircleTargetChoiceBox;

    @FXML
    private Label CheckCirclesLabel;

    @FXML
    private Label ChooseCheckCircleLabel;

    @FXML
    private ListView<String> CirclesListView;

    @FXML
    private AnchorPane WhatIfAnchorPane;

    @FXML
    private ChoiceBox<String> WhatIfChoiceBox;

    @FXML
    private Label WhatIFLabel;

    @FXML
    private Label ChooseWhatIfTarget;

    @FXML
    private ListView<String> WhatIfListView;

    @FXML
    private RadioButton DependsOnRadioButton;

    @FXML
    private ListView<String> showConnectionBetweenListView;

    @FXML
    private ToggleGroup What_If_Value;

    @FXML
    private RadioButton RequiredForRadioButton;

    //--------------------------------------------------Settings-----------------------------------------------------//
    public void setGraph(Graph graph)
    {
        this.graph = graph;

        setAllTargetsList();
        setRelationChoiceBox();
        SetTooltips();
    }

    private void setRelationChoiceBox()
    {
        relationsList.add(0, "Depends on");
        relationsList.add(1, "Required for");

        RelationChoiceBox.setItems(relationsList);
    }

    private void setAllTargetsList()
    {
        int i = 0;
        for(Target currentTargetName : graph.getGraphTargets().values())
            allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = allTargetsList.sorted();

        OriginTargetChoiceBox.setItems(sorted);
        CircleTargetChoiceBox.setItems(sorted);
        WhatIfChoiceBox.setItems(sorted);
    }

    private void SetTooltips()
    {
        OriginTargetChoiceBox.setTooltip(new Tooltip("Choose an origin target"));
        RelationChoiceBox.setTooltip(new Tooltip("Choose a relation between the targets"));
        DestinationTargetChoiceBox.setTooltip(new Tooltip("Choose a destination target"));

        CircleTargetChoiceBox.setTooltip(new Tooltip("Choose a target"));

        WhatIfChoiceBox.setTooltip(new Tooltip("Choose a target"));
    }

    //--------------------------------------------Targets Connection-----------------------------------------------//
    public void OriginTargetChosen(ActionEvent actionEvent) {
        originTargetName = OriginTargetChoiceBox.getValue();
        RelationChoiceBox.setDisable(false);
        DestinationTargetChoiceBox.setDisable(true);
        destinationTargets.clear();
        showConnectionBetweenListView.getItems().clear();
    }

    public void RelationChosen(ActionEvent actionEvent) {
        this.relation = RelationChoiceBox.getValue();
        Set<String> destinationSet;
        int i = 0;

        if(Objects.equals(relation, RelationChoiceBox.getItems().get(0)))
            destinationSet = graph.getTarget(this.originTargetName).getAllDependsOnTargets();
        else
            destinationSet = graph.getTarget(this.originTargetName).getAllRequiredForTargets();

        destinationTargets.clear();
        for(String currentTargetName : destinationSet)
            destinationTargets.add(i++, currentTargetName);

        DestinationTargetChoiceBox.setItems(destinationTargets.sorted());
        DestinationTargetChoiceBox.setDisable(false);
    }

    public void DestinationTargetChosen(ActionEvent actionEvent) {
        Target origin,destination;
        ArrayList<String> paths = new ArrayList<String>();
        ObservableList<String> pathsTargets = FXCollections.observableArrayList();
        PathFinder pathFinder = new PathFinder();

        destinationTargetName = DestinationTargetChoiceBox.getValue();
        origin = graph.getTarget(originTargetName);
        destination = graph.getTarget(destinationTargetName);

       if(Objects.equals(relation, RelationChoiceBox.getItems().get(0)))
           paths = pathFinder.getPathsFromTargets(origin,destination, Target.Connection.DEPENDS_ON);
       else
           paths = pathFinder.getPathsFromTargets(origin,destination, Target.Connection.REQUIRED_FOR);

        pathsTargets.addAll(paths);

        showConnectionBetweenListView.setItems(pathsTargets);
    }

    //--------------------------------------------------Circle-----------------------------------------------------//
    public void CheckIfTargetCircled(ActionEvent actionEvent) {
        CircleFinder circleFinder = new CircleFinder();
        circleFinder.checkIfCircled(graph.getTarget(CircleTargetChoiceBox.getValue()));

        String circlePath = circleFinder.getCirclePath();
        ObservableList<String> circleList = FXCollections.observableArrayList();

        if(circlePath == null)
            circleList.add("Not in a circle");
        else
            circleList.addAll(Arrays.asList(circlePath.split(" ")));

        CirclesListView.setItems(circleList);
    }

    public void WhatIfTargetSelected(ActionEvent actionEvent)
    {
        String selectedTarget = WhatIfChoiceBox.getValue();
        ObservableList<String> otherTargets = FXCollections.observableArrayList();

        if(DependsOnRadioButton.isSelected())
            otherTargets.addAll(graph.getTarget(selectedTarget).getAllDependsOnTargets());
        else if(RequiredForRadioButton.isSelected())
            otherTargets.addAll(graph.getTarget(selectedTarget).getAllRequiredForTargets());

        WhatIfListView.setItems(otherTargets);
    }

    //--------------------------------------------------What-if-----------------------------------------------------//
    public void WhatIfRadioButtonSelected(ActionEvent actionEvent) {
        if(WhatIfChoiceBox.getValue() != null)
            WhatIfTargetSelected(actionEvent);
    }
}
