package controllers;

import information.GraphPositionsInformation;
import information.TargetDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import target.Graph;
import target.Target;

public class ShowDetailsController {
    private Graph graph = null;
    private final ObservableList<TargetDetails> targetDetailsList = FXCollections.observableArrayList();
    private final ObservableList<GraphPositionsInformation> graphPositionsList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsInformationList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeGraphDetails();
        initializeGraphPositions();
        initializeSerialSetChoiceBox();
    }

    private void initializeGraphDetails() {
        TargetNumber.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("num"));
        TargetName.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("targetName"));
        TargetPosition.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("position"));
        TargetDirectDependsOn.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("directDependsOn"));
        TargetAllDependsOn.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("allDependsOn"));
        TargetDirectRequiredFor.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("directRequiredFor"));
        TargetAllRequiredFor.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("allRequiredFor"));
        TargetSerialSets.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("serialSets"));
        TargetExtraInformation.setCellValueFactory(new PropertyValueFactory<TargetDetails,String>("extraInformation"));
    }

    private void initializeGraphPositions() {
        RootsProperty.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("roots"));
        MiddlesProperty.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("middles"));
        LeavesProperty.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("leaves"));
        IndependentsProperty.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("independents"));
    }

    private void initializeSerialSetChoiceBox() {
        TargetSerialSetChoiceBox.setOnAction((event) -> {
            serialSetsInformationList.clear();
            serialSetsInformationList.addAll(graph.getSerialSetsMap().get(TargetSerialSetChoiceBox.getValue()));
            SerialSetsListsView.setItems(serialSetsInformationList.sorted());
        });
    }

    @FXML
    private AnchorPane AnchorPane;

    @FXML
    private ChoiceBox<String> TargetSerialSetChoiceBox;

    @FXML
    private ListView<String> SerialSetsListsView;

    @FXML
    private TableView<TargetDetails> TargetsDetailsTable;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetNumber;

    @FXML
    private TableColumn<TargetDetails, String> TargetName;

    @FXML
    private TableColumn<TargetDetails, String> TargetPosition;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetDirectDependsOn;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetAllDependsOn;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetDirectRequiredFor;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetAllRequiredFor;

    @FXML
    private TableColumn<TargetDetails, Integer> TargetSerialSets;

    @FXML
    private TableView<GraphPositionsInformation> TargetPositionsTable;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> RootsProperty;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> MiddlesProperty;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> LeavesProperty;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> IndependentsProperty;

    @FXML
    private TableColumn<TargetDetails, String> TargetExtraInformation;

    @FXML
    private Label SerialSetsLabel;

    @FXML
    private Label GraphProperties;

    @FXML
    private PieChart PropertiesPie;


    public void setGraph(Graph graph) {
        this.graph = graph;
        setTargetDetailsTable();
        setGraphPositionsTable();
        setTargetSerialSetChoiceBox();
    }

    private void setTargetDetailsTable() {
        TargetDetails currentTargetDetails;
        int i = 1;

        for (Target currentTarget : graph.getGraphTargets().values()) {
            currentTargetDetails = new TargetDetails(i, currentTarget.getTargetName(), currentTarget.getTargetPosition().toString(),
                    currentTarget.getDependsOnTargets().size(), currentTarget.getAllDependsOnTargets().size(),
                    currentTarget.getRequiredForTargets().size(), currentTarget.getAllRequiredForTargets().size(), currentTarget.getSerialSets().size(), currentTarget.getExtraInformation());

            targetDetailsList.add(currentTargetDetails);
            ++i;
        }

        TargetsDetailsTable.setItems(targetDetailsList);
    }

    private void setGraphPositionsTable()
    {
        GraphPositionsInformation graphPositionsInformation = new GraphPositionsInformation(
                graph.numberOfTargetsByProperty(Target.TargetPosition.ROOT),
                graph.numberOfTargetsByProperty(Target.TargetPosition.MIDDLE),
                graph.numberOfTargetsByProperty(Target.TargetPosition.LEAF),
                graph.numberOfTargetsByProperty(Target.TargetPosition.INDEPENDENT)
        );

        graphPositionsList.clear();
        graphPositionsList.add(graphPositionsInformation);
        TargetPositionsTable.setItems(graphPositionsList);
    }

    private void setTargetSerialSetChoiceBox()
    {
        int i = 0;
        for(String currentSerialSetName : graph.getSerialSetsNames())
            serialSetsNameList.add(i++, currentSerialSetName);

        TargetSerialSetChoiceBox.setItems(serialSetsNameList.sorted());
        TargetSerialSetChoiceBox.setTooltip(new Tooltip("Choose a serial set"));
    }
}
