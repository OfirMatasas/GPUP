package controllers;

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
import tableItems.AdminGraphDetailsTableItem;
import tableItems.AdminGraphPositionsTableItem;
import target.Graph;
import target.Target;

import javax.imageio.ImageIO;
import java.io.*;

public class AdminGraphDetailsController {
    //------------------------------------------------- Members ----------------------------------------------------//
    private Graph graph = null;
    private final ObservableList<AdminGraphDetailsTableItem> adminGraphDetailsTableItemList = FXCollections.observableArrayList();
    private final ObservableList<AdminGraphPositionsTableItem> graphPositionsList = FXCollections.observableArrayList();
    private final ObservableList<String> serialSetsInformationList = FXCollections.observableArrayList();
    private String directoryPath;

    //---------------------------------------------- FXML Members --------------------------------------------------//
    @FXML private AnchorPane AnchorPane;
    @FXML private TableView<AdminGraphDetailsTableItem> TargetsDetailsTable;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetNumber;
    @FXML private TableColumn<AdminGraphDetailsTableItem, String> TargetName;
    @FXML private TableColumn<AdminGraphDetailsTableItem, String> TargetPosition;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetDirectDependsOn;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetAllDependsOn;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetDirectRequiredFor;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetAllRequiredFor;
    @FXML private TableColumn<AdminGraphDetailsTableItem, Integer> TargetSerialSets;
    @FXML private TableView<AdminGraphPositionsTableItem> TargetPositionsTable;
    @FXML private TableColumn<AdminGraphPositionsTableItem, Integer> RootsPosition;
    @FXML private TableColumn<AdminGraphPositionsTableItem, Integer> MiddlesPosition;
    @FXML private TableColumn<AdminGraphPositionsTableItem, Integer> LeavesPosition;
    @FXML private TableColumn<AdminGraphPositionsTableItem, Integer> IndependentsPosition;
    @FXML private TableColumn<AdminGraphDetailsTableItem, String> TargetExtraInformation;
    @FXML private PieChart PositionsPie;
    @FXML private ImageView graphImage;

    //------------------------------------------------ Settings ----------------------------------------------------//
    @FXML public void initialize() {
        initializeGraphDetails();
        initializeGraphPositions();
    }

    private void initializeGraphDetails() {
        this.TargetNumber.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("num"));
        this.TargetName.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, String>("targetName"));
        this.TargetPosition.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, String>("position"));
        this.TargetDirectDependsOn.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("directDependsOn"));
        this.TargetAllDependsOn.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("allDependsOn"));
        this.TargetDirectRequiredFor.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("directRequiredFor"));
        this.TargetAllRequiredFor.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("allRequiredFor"));
        this.TargetSerialSets.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, Integer>("serialSets"));
        this.TargetExtraInformation.setCellValueFactory(new PropertyValueFactory<AdminGraphDetailsTableItem, String>("extraInformation"));
    }

    private void initializeGraphPositions() {
        this.RootsPosition.setCellValueFactory(new PropertyValueFactory<AdminGraphPositionsTableItem, Integer>("roots"));
        this.MiddlesPosition.setCellValueFactory(new PropertyValueFactory<AdminGraphPositionsTableItem, Integer>("middles"));
        this.LeavesPosition.setCellValueFactory(new PropertyValueFactory<AdminGraphPositionsTableItem, Integer>("leaves"));
        this.IndependentsPosition.setCellValueFactory(new PropertyValueFactory<AdminGraphPositionsTableItem, Integer>("independents"));
    }

    //------------------------------------------------ Graphviz ----------------------------------------------------//
    @FXML private Button saveGraphButton;

    @FXML private void saveGraphButtonToUserSelection(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG file (*.PNG)", "*.PNG"));
        fileChooser.setInitialFileName("GeneratedGraph".concat(".PNG"));
        File file = fileChooser.showSaveDialog(this.AnchorPane.getParent().getScene().getWindow());
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

    public void setGraph(Graph graph) throws FileNotFoundException {
        this.graph = graph;
        setTargetDetailsTable();
        setGraphPositionsTable();
        initializePie();
        convertXMLToDot(true);
        setGraphImage(this.directoryPath + "\\" + "GeneratedGraph.png");
    }

    public void setGraphImage(String fullFileName) throws FileNotFoundException {
        InputStream stream = new FileInputStream(fullFileName);
        Image image = new Image(stream);
        this.graphImage.fitWidthProperty().bind(this.AnchorPane.widthProperty());
        this.graphImage.setImage(image);
    }

    public void convertXMLToDot(boolean waitForProcess) {

        String currentColor;
        Target.TargetPosition targetPosition;
        String fileNameDOT = "GeneratedGraph.dot";
        String fileNamePNG = "GeneratedGraph.png";
        this.directoryPath = "C:\\gpup-working-dir";
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

    private String printAllDependsOnTarget(Target curTarget) {
        String DependedTarget = "";
        for (Target dependsOnTarget : curTarget.getDependsOnTargets())
        {
            DependedTarget = DependedTarget + dependsOnTarget.getTargetName() + " ";
        }
        return DependedTarget;
    }

    //-------------------------------------------- Graph Tableview -------------------------------------------------//
    private void setTargetDetailsTable() {
        AdminGraphDetailsTableItem currentAdminGraphDetailsTableItem;
        int i = 1;

        for (Target currentTarget : this.graph.getGraphTargets().values()) {
            currentAdminGraphDetailsTableItem = new AdminGraphDetailsTableItem(i, currentTarget.getTargetName(), currentTarget.getTargetPosition().toString(),
                    currentTarget.getDependsOnTargets().size(), currentTarget.getAllDependsOnTargets().size(),
                    currentTarget.getRequiredForTargets().size(), currentTarget.getAllRequiredForTargets().size(), currentTarget.getExtraInformation());

            this.adminGraphDetailsTableItemList.add(currentAdminGraphDetailsTableItem);
            ++i;
        }

        this.TargetsDetailsTable.setItems(this.adminGraphDetailsTableItemList);
    }

    //------------------------------------------ Positions Tableview -----------------------------------------------//
    private void setGraphPositionsTable() {
        AdminGraphPositionsTableItem adminGraphPositionsTableItem = new AdminGraphPositionsTableItem(
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.ROOT),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.MIDDLE),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.LEAF),
                this.graph.numberOfTargetsByProperty(Target.TargetPosition.INDEPENDENT)
        );

        this.graphPositionsList.clear();
        this.graphPositionsList.add(adminGraphPositionsTableItem);
        this.TargetPositionsTable.setItems(this.graphPositionsList);
    }

    //-------------------------------------------------- Pie -------------------------------------------------------//
    public void initializePie() {
        final double roots = this.graph.numberOfTargetsByProperty(Target.TargetPosition.ROOT);
        final double middles = this.graph.numberOfTargetsByProperty(Target.TargetPosition.MIDDLE);
        final double independents = this.graph.numberOfTargetsByProperty(Target.TargetPosition.INDEPENDENT);
        final double leaves = this.graph.numberOfTargetsByProperty(Target.TargetPosition.LEAF);
        final double sum = roots + middles + independents + leaves;

        ObservableList<Data> pieChartData= FXCollections.observableArrayList(
        new Data("Roots" , roots),
        new Data("Middles" ,middles),
        new Data("Independents" , independents),
        new Data("Leaves" ,leaves));

        this.PositionsPie.setData(pieChartData);
        this.PositionsPie.setTitle("Graph Position Pie");
        final String[] percentage = new String[1];
        this.PositionsPie.getData().forEach(data -> {
                    percentage[0] = (String.format("%.2f%%", data.getPieValue()/sum*100));
                    Tooltip tooltip = new Tooltip(percentage[0]);
                    Tooltip.install(data.getNode(), tooltip);
                });
    }
}