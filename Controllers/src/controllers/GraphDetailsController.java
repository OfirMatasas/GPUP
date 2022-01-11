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
import java.nio.file.Files;

public class GraphDetailsController {
    private Graph graph = null;
    private final ObservableList<TargetDetails> targetDetailsList = FXCollections.observableArrayList();
    private final ObservableList<GraphPositionsInformation> graphPositionsList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsInformationList = FXCollections.observableArrayList();
    private FileWriter dotFile;
    private String directoryPath;
    private  GraphSummary graphSummary;

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
        TargetExtraInformation.setCellValueFactory(new PropertyValueFactory<TargetDetails, String>("extraInformation"));
    }

    private void initializeGraphPositions() {
        RootsPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("roots"));
        MiddlesPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("middles"));
        LeavesPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("leaves"));
        IndependentsPosition.setCellValueFactory(new PropertyValueFactory<GraphPositionsInformation, Integer>("independents"));
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
    private TableColumn<GraphPositionsInformation, Integer> RootsPosition;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> MiddlesPosition;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> LeavesPosition;

    @FXML
    private TableColumn<GraphPositionsInformation, Integer> IndependentsPosition;

    @FXML
    private TableColumn<TargetDetails, String> TargetExtraInformation;

    @FXML
    private Label SerialSetsLabel;

    @FXML
    private Label GraphPositions;

    @FXML
    private PieChart PositionsPie;

    @FXML private ImageView graphImage;

    @FXML private ScrollPane graphImageScrollPane;



    @FXML
    private Button saveGraphButton;

    @FXML
    void saveGraphButtonToUserSelection(ActionEvent event) throws IOException
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG file (*.PNG)", "*.PNG"));
        fileChooser.setInitialFileName("GeneratedGraph".concat(".PNG"));

        File file = fileChooser.showSaveDialog(AnchorPane.getParent().getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(graphImage.getImage(),
                        null), "PNG", file);

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            this.directoryPath = file.getParent();
            convertXMLToDot();
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
        convertXMLToDot();
        setGraphImage(directoryPath + "\\" + "GeneratedGraph.png");
    }

    public void setGraphImage(String fullFileName) throws FileNotFoundException {
        InputStream stream = new FileInputStream(fullFileName);
        Image image = new Image(stream);
        graphImage.fitWidthProperty().bind(AnchorPane.widthProperty());
        graphImage.setImage(image);
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

    private void setGraphPositionsTable() {
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

    private void setTargetSerialSetChoiceBox() {
        int i = 0;
        for (String currentSerialSetName : graph.getSerialSetsNames())
            serialSetsNameList.add(i++, currentSerialSetName);

        TargetSerialSetChoiceBox.setItems(serialSetsNameList.sorted());
        TargetSerialSetChoiceBox.setTooltip(new Tooltip("Choose a serial set"));
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

        PositionsPie.setData(pieChartData);
        PositionsPie.setTitle("Graph Position Pie");
        final String[] percentage = new String[1];
        PositionsPie.getData().forEach(data -> {
                    percentage[0] = (String.format("%.2f%%", data.getPieValue()/sum*100));
                    Tooltip tooltip = new Tooltip(percentage[0]);
                    Tooltip.install(data.getNode(), tooltip);
                });
    }

    public void setDotFile(FileWriter dotFile)
    {
        this.dotFile = dotFile;
    }

    public void convertXMLToDot() {

        String currentColor;
        Target.TargetPosition targetPosition;
        String fileNameDOT = "GeneratedGraph.dot";
        String fileNamePNG = "GeneratedGraph.png";
        String workingDirectory = graphSummary.getWorkingDirectory();
        String createPNGFromDOT = "dot -Tpng "+ fileNameDOT + " -o " + fileNamePNG;
        String properties = "digraph G {\n" + "node [margin=0 fontcolor=black fontsize=28 width=1 shape=circle style=filled]\n" +
                "\n" +
                "nodesep = 2;\n" +
                "ranksep = 2;\n";

        try {
            dotFile = new FileWriter(new File(directoryPath,fileNameDOT));
            dotFile.write(properties);

            for (Target target : graph.getGraphTargets().values())
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


            for (Target target : graph.getGraphTargets().values()) {
                dotFile.write(target.getTargetName());

                if (!target.getDependsOnTargets().isEmpty())
                    dotFile.write("-> {" + printAllDependsOnTarget(target) + "}");
                dotFile.write("\n");

//                dotFile.write("\n");
            }
            dotFile.write("}");
            dotFile.close();

            Process process = Runtime.getRuntime().exec("cmd /c cmd.exe /K \"cd \\ && cd " + directoryPath + " && " + createPNGFromDOT + " && exit");
            process.waitFor();
        }
        catch(IOException | InterruptedException e) {
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

