package controllers;

import information.GraphPositionsInformation;
import information.TargetDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import summaries.GraphSummary;
import target.Graph;
import target.Target;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class GraphDetailsController {
    private Graph graph = null;
    private final ObservableList<TargetDetails> targetDetailsList = FXCollections.observableArrayList();
    private final ObservableList<GraphPositionsInformation> graphPositionsList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsInformationList = FXCollections.observableArrayList();
    private String directoryPath;
    private  GraphSummary graphSummary;

    @FXML public void initialize() {
        initializeGraphDetails();
        initializeGraphPositions();
        initializeSerialSetChoiceBox();
    }

    private void initializeGraphDetails() {
        this.TargetNumber.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("num"));
        this.TargetName.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("targetName"));
        this.TargetPosition.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("position"));
        this.TargetDirectDependsOn.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("directDependsOn"));
        this.TargetAllDependsOn.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("allDependsOn"));
        this.TargetDirectRequiredFor.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("directRequiredFor"));
        this.TargetAllRequiredFor.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("allRequiredFor"));
        this.TargetSerialSets.setCellValueFactory(new PropertyValueFactory<TargetDetails, Integer>("serialSets"));
        this.TargetExtraInformation.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("extraInformation"));
    }

    private void initializeGraphPositions() {
        this.RootsPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("roots"));
        this.MiddlesPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("middles"));
        this.LeavesPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("leaves"));
        this.IndependentsPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("independents"));
    }

    private void initializeSerialSetChoiceBox() {
        this.TargetSerialSetChoiceBox.setOnAction((event) -> {
            this.serialSetsInformationList.clear();
            this.serialSetsInformationList.addAll(this.graph.getSerialSetsMap().get(this.TargetSerialSetChoiceBox.getValue()));
            this.SerialSetsListsView.setItems(this.serialSetsInformationList.sorted());
        });
    }

    @FXML private ChoiceBox<String> TargetSerialSetChoiceBox;
    @FXML private ListView<String> SerialSetsListsView;
    @FXML private TableView<TargetDetails> TargetsDetailsTable;
    @FXML private TableColumn<TargetDetails, Integer> TargetNumber;
    @FXML private TableColumn<TargetDetails, String> TargetName;
    @FXML private TableColumn<TargetDetails, String> TargetPosition;
    @FXML private TableColumn<TargetDetails, Integer> TargetDirectDependsOn;
    @FXML private TableColumn<TargetDetails, Integer> TargetAllDependsOn;
    @FXML private TableColumn<TargetDetails, Integer> TargetDirectRequiredFor;
    @FXML private TableColumn<TargetDetails, Integer> TargetAllRequiredFor;
    @FXML private TableColumn<TargetDetails, Integer> TargetSerialSets;
    @FXML private TableView<GraphPositionsInformation> TargetPositionsTable;
    @FXML private TableColumn<GraphPositionsInformation, Integer> RootsPosition;
    @FXML private TableColumn<GraphPositionsInformation, Integer> MiddlesPosition;
    @FXML private TableColumn<GraphPositionsInformation, Integer> LeavesPosition;
    @FXML private TableColumn<GraphPositionsInformation, Integer> IndependentsPosition;
    @FXML private TableColumn<TargetDetails, String> TargetExtraInformation;
    @FXML private Label SerialSetsLabel;
    @FXML private Label GraphPositions;
    @FXML private PieChart PositionsPie;
    @FXML private ImageView graphImage;
    @FXML private TreeView<String> TargetTreeView;
    @FXML private Button saveGraphButton;
    @FXML private TabPane graphDetailsTabPane;

    @FXML private void saveGraphButtonToUserSelection(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG file (*.PNG)", "*.PNG"));
        fileChooser.setInitialFileName("GeneratedGraph".concat(".PNG"));
        File file = fileChooser.showSaveDialog(this.graphDetailsTabPane.getParent().getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(this.graphImage.getImage(),
                        null), "PNG", file);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            this.directoryPath = file.getParent();
            convertXMLToDot(false);
        }
    }

    public void setGraph(Graph graph, GraphSummary graphSummary) throws FileNotFoundException {
        this.graph = graph;
        this.graphSummary = graphSummary;
        this.directoryPath = this.graphSummary.getWorkingDirectory();
        setTargetDetailsTable();
        setGraphPositionsTable();
        setTargetSerialSetChoiceBox();
        initializePie();
        convertXMLToDot(true);
        setGraphImage(this.directoryPath + "\\" + "GeneratedGraph.png");
        createTreeViewOfGraph();
    }

    private void createTreeViewOfGraph() {
        TreeItem<String> graphAsItem = new TreeItem<> (this.graph.getGraphName());
        Set<String> targetsAddedSoFar;

        for(Target currTarget : this.graph.getGraphTargets().values())
        {
            if(currTarget.getTargetPosition().equals(Target.TargetPosition.ROOT) || currTarget.getTargetPosition().equals(Target.TargetPosition.INDEPENDENT))
            {
                TreeItem<String> rootInGraph = new TreeItem<>(currTarget.getTargetName());
                graphAsItem.getChildren().add(rootInGraph);

                targetsAddedSoFar = new HashSet<>();
                targetsAddedSoFar.add(currTarget.getTargetName());
                createTreeViewOfGraphRec(rootInGraph, currTarget, targetsAddedSoFar);
            }
        }

        this.TargetTreeView.setRoot(graphAsItem);
    }

    private void createTreeViewOfGraphRec(TreeItem<String> currItem, Target currTarget, Set<String> targetsAddedSoFar) {
        if(currTarget.getDependsOnTargets().isEmpty())
            return;

        TreeItem<String> dependedItem;
        Set<String> newAddedTargets;

        for(Target dependedTarget : currTarget.getDependsOnTargets())
        {
            dependedItem = new TreeItem<>(currTarget.getTargetName());
            currItem.getChildren().add(dependedItem);

            if(!targetsAddedSoFar.contains(dependedTarget.getTargetName()))
            {
                newAddedTargets = new HashSet<>(targetsAddedSoFar);
                newAddedTargets.add(dependedTarget.getTargetName());
                createTreeViewOfGraphRec(dependedItem, dependedTarget, newAddedTargets);
            }
        }
    }

    public void setGraphImage(String fullFileName) throws FileNotFoundException {
        InputStream stream = new FileInputStream(fullFileName);
        Image image = new Image(stream);
        this.graphImage.setImage(image);
    }

    private void setTargetDetailsTable() {
        TargetDetails currentTargetDetails;
        int i = 1;

        for (Target currentTarget : this.graph.getGraphTargets().values()) {
            currentTargetDetails = new TargetDetails(i, currentTarget.getTargetName(), currentTarget.getTargetPosition().toString(),
                    currentTarget.getDependsOnTargets().size(), currentTarget.getAllDependsOnTargets().size(),
                    currentTarget.getRequiredForTargets().size(), currentTarget.getAllRequiredForTargets().size(), currentTarget.getSerialSets().size(), currentTarget.getExtraInformation());

            this.targetDetailsList.add(currentTargetDetails);
            ++i;
        }

        this.TargetsDetailsTable.setItems(this.targetDetailsList);
    }

    private void setGraphPositionsTable() {
        GraphPositionsInformation graphPositionsInformation = new GraphPositionsInformation(
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.ROOT),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.MIDDLE),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.LEAF),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.INDEPENDENT)
        );

        this.graphPositionsList.clear();
        this.graphPositionsList.add(graphPositionsInformation);
        this.TargetPositionsTable.setItems(this.graphPositionsList);
    }

    private void setTargetSerialSetChoiceBox() {
        int i = 0;
        for (String currentSerialSetName : this.graph.getSerialSetsNames())
            this.serialSetsNameList.add(i++, currentSerialSetName);

        this.TargetSerialSetChoiceBox.setItems(this.serialSetsNameList.sorted());
        this.TargetSerialSetChoiceBox.setTooltip(new Tooltip("Choose a serial set"));
    }

    public void initializePie() {

        double roots,middles,independents,leaf;
        roots= this.graph.numberOfTargetsByProperty(Target.TargetPosition.ROOT);
        middles= this.graph.numberOfTargetsByProperty(Target.TargetPosition.MIDDLE);
        independents= this.graph.numberOfTargetsByProperty(Target.TargetPosition.INDEPENDENT);
        leaf= this.graph.numberOfTargetsByProperty(Target.TargetPosition.LEAF);
        final double sum = roots + middles + independents + leaf;

        ObservableList<Data> pieChartData= FXCollections.observableArrayList(
        new Data("Roots" , roots),
        new Data("Middles" ,middles),
        new Data("Independents" , independents),
        new Data("Leaves" ,leaf));

        this.PositionsPie.setData(pieChartData);
        this.PositionsPie.setTitle("Graph Position Pie");
        final String[] percentage = new String[1];
        this.PositionsPie.getData().forEach(data -> {
                    percentage[0] = (String.format("%.2f%%", data.getPieValue()/sum*100));
                    Tooltip tooltip = new Tooltip(percentage[0]);
                    Tooltip.install(data.getNode(), tooltip);
                });
    }

    public void convertXMLToDot(boolean waitForProcess) {

        String currentColor;
        Target.TargetPosition targetPosition;
        String fileNameDOT = "GeneratedGraph.dot";
        String fileNamePNG = "GeneratedGraph.png";
        String workingDirectory = this.graphSummary.getWorkingDirectory();
        String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
        String properties = "digraph G {\n" + "bgcolor=transparent \n" +  "node [margin=0 fontcolor=black fontsize=28 width=1 shape=circle style=filled]\n" +
                "\n" +
                "nodesep = 2;\n" +
                "ranksep = 2;\n";

        try {
            FileWriter dotFile = new FileWriter(new File(this.directoryPath, fileNameDOT));
            dotFile.write(properties);

            for (Target target : this.graph.getGraphTargets().values())
            {
                dotFile.write(target.getTargetName());

                targetPosition = target.getTargetPosition();
                if(targetPosition.equals(Target.TargetPosition.ROOT))
                    currentColor = "dodgerblue";
                else if(targetPosition.equals(Target.TargetPosition.MIDDLE))
                    currentColor = "Gold";
                else if(targetPosition.equals(Target.TargetPosition.LEAF))
                    currentColor = "green3";
                else
                    currentColor = "chocolate1";

                dotFile.write(" [fillcolor = " +  currentColor + "]\n");
            }
            dotFile.write("rankdir=TB; \n");
            dotFile.write("Roots [shape=plaintext fillcolor=dodgerblue]\n");
            dotFile.write("Middles [shape=plaintext fillcolor=Gold]\n");
            dotFile.write("Leaves [shape=plaintext fillcolor=green3]\n");
            dotFile.write("Independents [shape=plaintext fillcolor=chocolate1]\n");

            for (Target target : this.graph.getGraphTargets().values()) {
                dotFile.write(target.getTargetName());

                if (!target.getDependsOnTargets().isEmpty())
                    dotFile.write("-> {" + printAllDependsOnTarget(target) + "}");
                dotFile.write("\n");
            }

            dotFile.write("}");
            dotFile.close();

            Process process = Runtime.getRuntime().exec("cmd /c cmd.exe /K \"cd \\ && cd " + this.directoryPath + " && " + createPNGFromDOT + " && exit");
            if(waitForProcess)
                process.waitFor();
        } catch(IOException | InterruptedException e) {
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
}