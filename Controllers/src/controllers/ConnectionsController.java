package controllers;

import graphAnalyzers.CircleFinder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import target.Graph;
import target.Target;

import java.util.Arrays;
import java.util.Set;

public class ConnectionsController {
    private Graph graph = null;
    private final ObservableList<String> allTargetsList = FXCollections.observableArrayList();
    private final ObservableList<String> relationsList = FXCollections.observableArrayList();
    private final ObservableList<String> destinationTargets = FXCollections.observableArrayList();
    private String originTargetName;
    private String relation;

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
    private ListView<?> ConnectionsListView;

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

    public void setGraph(Graph graph)
    {
        this.graph = graph;

        setAllTargetsList();
        setRelationChoiceBox();
        setCircleListView();
    }

    private void setCircleListView() {
    }

    private void setRelationChoiceBox()
    {
        relationsList.add(0, "Depends On");
        relationsList.add(1, "Required For");

        RelationChoiceBox.setItems(relationsList);
        OriginTargetChoiceBox.setTooltip(new Tooltip("Choose relation between targets"));
    }

    private void setAllTargetsList()
    {
        int i = 0;
        for(Target currentTargetName : graph.getGraphTargets().values())
            allTargetsList.add(i++, currentTargetName.getTargetName());

        OriginTargetChoiceBox.setItems(allTargetsList.sorted());
        OriginTargetChoiceBox.setTooltip(new Tooltip("Choose an origin target"));

        CircleTargetChoiceBox.setItems(allTargetsList.sorted());
        CircleTargetChoiceBox.setTooltip(new Tooltip("Choose a target"));
    }

    public void OriginTargetChosen(ActionEvent actionEvent) {
        this.originTargetName = (String)OriginTargetChoiceBox.getValue();
        RelationChoiceBox.setValue(null);
        RelationChoiceBox.setDisable(false);
        DestinationTargetChoiceBox.setDisable(true);
        destinationTargets.clear();
    }

    public void RelationChosen(ActionEvent actionEvent) {
        this.relation = RelationChoiceBox.getValue();
        int i = 0;
        Set<String> destinationSet;

        if(relation == "Depends On")
            destinationSet = graph.getTarget(this.originTargetName).getAllDependsOnTargets();
        else
            destinationSet = graph.getTarget(this.originTargetName).getAllRequiredForTargets();

        destinationTargets.clear();
        for(String currentTargetName : destinationSet)
            destinationTargets.add(i++, currentTargetName);

        DestinationTargetChoiceBox.setItems(destinationTargets.sorted());
        DestinationTargetChoiceBox.setTooltip(new Tooltip("Choose a destination target"));
        DestinationTargetChoiceBox.setDisable(false);
    }

    public void DestinationTargetChosen(ActionEvent actionEvent) {

    }

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
}
