package controllers;

import com.google.gson.Gson;
import http.HttpClientUtil;
import information.CreateTaskTargetInformation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import patterns.Patterns;
import target.Graph;
import target.Target;
import task.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AdminCreateTaskController implements Initializable{
    public TextArea taskDetailsOnTargetTextArea;
    private Graph graph;
    private String userName;
    private Gson gson;
    private Map<String, SimulationParameters> taskParametersMap = new HashMap<>();
    private SimulationParameters taskParameters;
    private final ObservableList<String> affectedTargetsOptions = FXCollections.observableArrayList();
    private final ObservableList<String> currentSelectedTargets = FXCollections.observableArrayList();
    private final String REQUIRED = "All required-for targets";
    private final String DEPENDED = "All depends-on targets";
    private final String SIMULATION ="Simulation";
    private final String COMPILATION ="Compilation";
    private final ObservableList<CreateTaskTargetInformation> taskTargetDetailsList = FXCollections.observableArrayList();
    private ObservableList<String> allTargetsList;
    private File sourceCodeDirectory = null;
    private File outputDirectory = null;
    private TaskThread.TaskType taskType;

    @FXML private ScrollPane scrollPane;
    @FXML private BorderPane taskBorderPane;
    @FXML private Pane leftPane;
    @FXML private ComboBox<String> taskSelection;
    @FXML private ComboBox<String> targetSelection;
    @FXML private ComboBox<String> affectedTargets;
    @FXML private ListView<String> currentSelectedTargetListView;
    @FXML private Label currentSelectedTargetLabel;
    @FXML private Label processingTimeLabel;
    @FXML private Label limitedPermanentLabel;
    @FXML private Label successRateLabel;
    @FXML private Label successRateWithWarnings;
    @FXML private RadioButton limitedRadioButton;
    @FXML private ToggleGroup limitedOrPermanent;
    @FXML private RadioButton permanentRadioButton;
    @FXML private Slider successRateSlider;
    @FXML private Slider successRateWithWarningsSlider;
    @FXML private TextField processingTimeTextField;
    @FXML private TextField successWithWarningRateText;
    @FXML private TextField successRateText;
    @FXML private Button ApplyParametersButton;
    @FXML private Button selectAllButton;
    @FXML private Button deselectAllButton;
    @FXML private Button addSelectedButton;
    @FXML private Label compilationSourceCodeLabel;
    @FXML private Label compilationOutputLabel;
    @FXML private Button toCompileButton;
    @FXML private Button compiledOutputButton;
    @FXML private Label sourceCodePathLabel;
    @FXML private Label outputPathLabel;
    @FXML private TableView<CreateTaskTargetInformation> taskTargetDetailsTableView;
    @FXML private TableColumn<CreateTaskTargetInformation, Integer> numberColumn;
    @FXML private TableColumn<CreateTaskTargetInformation, String> targetNameColumn;
    @FXML private TableColumn<CreateTaskTargetInformation, String> positionColumn;
    @FXML private Button removeSelectedButton;
    @FXML private Button clearTableButton;
    @FXML private Button CreateNewTaskButton;
    @FXML private TextField TaskNameTextField;

    @FXML void ApplyParametersToTask(ActionEvent event) {
        this.taskParameters = getSimulationTaskParametersFromUser();

        if(this.taskParameters.getProcessingTime() != null) // Checking only on the processing time, because other parameters are already initialized
            this.CreateNewTaskButton.setDisable(false);
    }

    @FXML void ClearTable(ActionEvent event) {
        this.taskTargetDetailsTableView.getItems().clear();
        this.CreateNewTaskButton.setDisable(true);
        this.clearTableButton.setDisable(true);
        this.removeSelectedButton.setDisable(true);
    }

    @FXML void CreateNewTaskButtonPressed(ActionEvent event) {
        String taskName = this.TaskNameTextField.getText();
        String uploader = this.userName;
        String graphName = this.graph.getGraphName();
        Set<String> targets = new HashSet<>();
        String taskTypeRequest = null;
        String stringObject = null;

        for (CreateTaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
            targets.add(curr.getTargetName());

        if(this.taskSelection.getValue().equals("Simulation"))
        {
            Integer pricing = this.graph.getTasksPricesMap().get(Graph.TaskType.Simulation);
            SimulationParameters parameters = this.taskParameters;

            SimulationTaskInformation taskInfo = new SimulationTaskInformation(taskName, uploader,
                    graphName, targets, pricing, parameters);
            taskTypeRequest = "Simulation";
            stringObject = this.gson.toJson(taskInfo);
        }
        else if(this.taskSelection.getValue().equals("Compilation"))
        {
            Integer pricing = this.graph.getTasksPricesMap().get(Graph.TaskType.Compilation);
            CompilationParameters parameters = new CompilationParameters(new File("null"), new File("null"));

            CompilationTaskInformation taskInfo = new CompilationTaskInformation(taskName, uploader, graphName, targets, pricing, parameters);
            taskTypeRequest = "Simulation";
            stringObject = this.gson.toJson(taskInfo);
        }

        if(!checkForValidCreationOfTask())
            return;

        uploadTaskToServer(stringObject, taskTypeRequest);
    }

    private void uploadTaskToServer(String stringObject, String taskTypeRequest) {
        RequestBody body = RequestBody.create(stringObject, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(Patterns.TASK)
                .post(body).addHeader(taskTypeRequest, taskTypeRequest)
                .build();

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("got task response - failed");
                Platform.runLater(()-> ShowPopUp(Alert.AlertType.ERROR, "Error in uploading task!", null, e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) {
                System.out.println("got task response - success");
                if(response.code() >= 200 && response.code() < 300)
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.INFORMATION, "Task uploaded successfully!", null, response.header("message")));
                else
                    Platform.runLater(() -> ShowPopUp(Alert.AlertType.ERROR, "Error in uploading task!", null, response.header("message")));
            }
        });
    }

    @FXML void addSelectedTargetsToTable(ActionEvent event)
    {
        setTaskTargetDetailsTable();

        if(this.taskType.equals(TaskThread.TaskType.Compilation))
        {
            if(this.outputDirectory != null && this.sourceCodeDirectory != null)
                this.CreateNewTaskButton.setDisable(false);
        }
        else
        {
            if(this.taskParameters != null)
                this.CreateNewTaskButton.setDisable(false);
        }
        this.clearTableButton.setDisable(false);
    }

    private void setTaskTargetDetailsTable()
    {
        int i = this.taskTargetDetailsTableView.getItems().size() + 1;
        String targetPosition, targetRuntimeStatus, targetResultStatus;
        CreateTaskTargetInformation taskTargetInformation;
        ObservableList<CreateTaskTargetInformation> tableList = this.taskTargetDetailsTableView.getItems();

        for(String currentTarget : this.currentSelectedTargetListView.getItems())
        {
            if(!targetExistedInTable(tableList, currentTarget)) {
                targetPosition = this.graph.getTarget(currentTarget).getTargetPosition().toString();
                taskTargetInformation = new CreateTaskTargetInformation(i++, currentTarget, targetPosition);
                this.taskTargetDetailsList.add(taskTargetInformation);
            }
        }
        this.taskTargetDetailsTableView.setItems(this.taskTargetDetailsList);
    }

    private boolean targetExistedInTable(ObservableList<CreateTaskTargetInformation> tableList, String currentTargetName) {
        for(CreateTaskTargetInformation currInfo : tableList)
        {
            if(currInfo.getTargetName().equals(currentTargetName))
                return true;
        }
        return false;
    }

    private Boolean checkForValidCreationOfTask()
    {
        String errorMessage = "";

        if(this.taskTargetDetailsTableView.getItems().isEmpty())
            errorMessage = "You have to choose at least 1 target first!";
        else if(this.TaskNameTextField.getText().trim().equals(""))
            errorMessage = "You have to name your task!";
        else if(this.taskType.equals(TaskThread.TaskType.Simulation))
        {
            if(this.taskParameters == null)
                errorMessage = "You have to apply the parameters for the task first!";
        }
        else //Compilation task
        {
            if(this.sourceCodeDirectory == null || this.outputDirectory == null)
                errorMessage = "Please choose directories for compilation task!";
        }

        if(!errorMessage.equals(""))
        {
            ShowPopup(errorMessage, "Can't start task");
            return false;
        }
        return true;
    }

    @FXML void affectedTargetsPressed(ActionEvent event) {
        Set<String> affectedTargetsSet = null;

        switch (this.affectedTargets.getValue())
        {
            case DEPENDED:
            {
                affectedTargetsSet = this.graph.getTarget(this.targetSelection.getValue()).getAllDependsOnTargets();
                break;
            }
            case REQUIRED:
            {
                affectedTargetsSet = this.graph.getTarget(this.targetSelection.getValue()).getAllRequiredForTargets();
                break;
            }
            default:
                break;
        }

        if(affectedTargetsSet != null)
        {
            this.currentSelectedTargets.clear();
            this.currentSelectedTargets.add(this.targetSelection.getValue());
            this.currentSelectedTargets.addAll(affectedTargetsSet);
        }
    }

    @FXML void chooseOutputDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        this.outputDirectory = directoryChooser.showDialog(this.taskBorderPane.getParent().getScene().getWindow());
        if(this.outputDirectory != null)
            this.outputPathLabel.setText("Output Path : " + this.outputDirectory.getAbsolutePath());

//        this.runButton.setDisable(this.sourceCodeDirectory == null || this.outputDirectory == null || this.taskTargetDetailsTableView.getItems().isEmpty());
    }

    @FXML void chooseSourceCodeDirectoryToCompile(ActionEvent event)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        this.sourceCodeDirectory = directoryChooser.showDialog(this.taskBorderPane.getParent().getScene().getWindow());
        if(this.sourceCodeDirectory != null)
            this.sourceCodePathLabel.setText("Source Code Path : " + this.sourceCodeDirectory.getAbsolutePath());

//        this.runButton.setDisable(this.sourceCodeDirectory == null || this.outputDirectory == null || this.taskTargetDetailsTableView.getItems().isEmpty());
    }

    @FXML void deselectAllPressed(ActionEvent event) {
        this.currentSelectedTargets.clear();
    }

    @FXML void removeSelectedRowFromTable(ActionEvent event)
    {
        if(this.taskTargetDetailsTableView.getItems().size()>0)
        {
            CreateTaskTargetInformation chosenTarget = this.taskTargetDetailsTableView.getSelectionModel().getSelectedItem();
            if(chosenTarget!=null) {
                int index = chosenTarget.getNumber() - 1, size = this.taskTargetDetailsTableView.getItems().size();

                this.taskTargetDetailsTableView.getItems().remove(chosenTarget);

                while (size - 1 > index) {
                    chosenTarget = this.taskTargetDetailsTableView.getItems().get(index);
                    chosenTarget.setNumber(++index);
                }

                if (size - 1 == 0) {
//                    this.runButton.setDisable(true);
                    this.clearTableButton.setDisable(false);
                }
            }
        }
        if(this.taskTargetDetailsTableView.getItems().isEmpty())
        {
            this.removeSelectedButton.setDisable(true);
            this.clearTableButton.setDisable(true);
        }
    }

    @FXML void selectAllPressed(ActionEvent event) {
        this.currentSelectedTargets.clear();
        this.graph.getGraphTargets().values().forEach(targetName -> this.currentSelectedTargets.add(targetName.getTargetName()));
    }

    @FXML void targetSelectionPressed(ActionEvent event) {
        this.affectedTargets.setDisable(false);

        this.currentSelectedTargets.clear();
        this.currentSelectedTargets.add(this.targetSelection.getValue());

        if(this.affectedTargets.getValue() != null)
            affectedTargetsPressed(event);
    }

    @FXML
    void taskSelectionPressed(ActionEvent event) {
        if(!this.taskSelection.getSelectionModel().isEmpty())
        {
            setForSimulationTask(!this.taskSelection.getValue().equals(this.SIMULATION));
            disableButtons(false);
        }

        this.taskType = this.taskSelection.getValue().equals("Simulation") ? TaskThread.TaskType.Simulation : TaskThread.TaskType.Compilation;
    }

    private void ShowPopup(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void enableTargetInfoTextArea(boolean flag) {
        this.taskDetailsOnTargetTextArea.setVisible(flag);
        this.taskDetailsOnTargetTextArea.setDisable(!flag);
    }


    private void disableButtons(Boolean flag) {

        this.targetSelection.setDisable(flag);
        this.affectedTargets.setDisable(flag);
        this.currentSelectedTargetLabel.setDisable(flag);
        this.currentSelectedTargetListView.setDisable(flag);

        this.selectAllButton.setDisable(flag);
    }

    private void setForSimulationTask(boolean flag) {
        this.processingTimeLabel.setDisable(flag);
        this.limitedPermanentLabel.setDisable(flag);
        this.successRateLabel.setDisable(flag);
        this.successRateWithWarnings.setDisable(flag);

        this.processingTimeTextField.setDisable(flag);
        this.limitedRadioButton.setDisable(flag);
        this.permanentRadioButton.setDisable(flag);

        this.successRateSlider.setDisable(flag);
        this.successRateWithWarningsSlider.setDisable(flag);

        this.successRateText.setDisable(flag);
        this.successWithWarningRateText.setDisable(flag);
        this.ApplyParametersButton.setDisable(flag);
    }

    public SimulationParameters getSimulationTaskParametersFromUser() {
        SimulationParameters taskParameters = new SimulationParameters();
        Duration processingTime;
        long timeInMS;
        boolean isRandom;
        double successRate, successWithWarnings;

        try {
            timeInMS = Integer.parseInt(this.processingTimeTextField.getText());
            processingTime = Duration.of(timeInMS, ChronoUnit.MILLIS);

            isRandom = this.limitedRadioButton.isSelected();
            successRate = this.successRateSlider.getValue();
            successWithWarnings = this.successRateWithWarningsSlider.getValue();

            taskParameters.setProcessingTime(processingTime);
            taskParameters.setRandom(isRandom);
            taskParameters.setSuccessRate(successRate);
            taskParameters.setSuccessWithWarnings(successWithWarnings);
        }
        catch(Exception ex)
        {
            ShowPopup("Invalid input in parameters.", "Invalid Parameters");
        }

        return taskParameters;
    }

    @FXML void permanentOptionPressed(MouseEvent event) {}

    private Set<String> setCurrentRunTargets() {
        Set<String> currentRunTargets = new HashSet<>();

        for(CreateTaskTargetInformation curr : this.taskTargetDetailsTableView.getItems())
            currentRunTargets.add(curr.getTargetName());

        return currentRunTargets;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> taskSelectionList = FXCollections.observableArrayList(this.SIMULATION, this.COMPILATION);
        this.taskSelection.setItems(taskSelectionList);

        addListenersForSliders();
        addListenersForTextFields();
        addListenersForSelectedTargets();
        addListenersToButtons();
        addListenersForCompilationButtons();

        this.affectedTargetsOptions.addAll("none", this.DEPENDED, this.REQUIRED);
        this.affectedTargets.setItems(this.affectedTargetsOptions);
        this.gson = new Gson();

        initializeGraphDetails();
    }

    private void initializeGraphDetails() {
        this.numberColumn.setCellValueFactory(new PropertyValueFactory<CreateTaskTargetInformation, Integer>("number"));
        this.targetNameColumn.setCellValueFactory(new PropertyValueFactory<CreateTaskTargetInformation, String>("targetName"));
        this.positionColumn.setCellValueFactory(new PropertyValueFactory<CreateTaskTargetInformation, String>("position"));
    }

    private void addListenersForSelectedTargets() {
        //Enable/Disable incremental, selectAll, deselectAll button
        this.currentSelectedTargets.addListener((ListChangeListener<String>) c -> {
            boolean containAll = AdminCreateTaskController.this.currentSelectedTargets.containsAll(AdminCreateTaskController.this.allTargetsList);
            AdminCreateTaskController.this.selectAllButton.setDisable(containAll);
            AdminCreateTaskController.this.deselectAllButton.setDisable(!containAll);

            while (c.next()) {
                for (String remitem : c.getRemoved()) {
                    AdminCreateTaskController.this.currentSelectedTargetListView.getItems().remove(remitem);
                    AdminCreateTaskController.this.addSelectedButton.setDisable(true);
                }
                for (String additem : c.getAddedSubList()) {
                    AdminCreateTaskController.this.currentSelectedTargetListView.getItems().add(additem);
                    AdminCreateTaskController.this.addSelectedButton.setDisable(false);
                }
            }

            if(AdminCreateTaskController.this.currentSelectedTargets.isEmpty())
                AdminCreateTaskController.this.addSelectedButton.setDisable(true);
        });
    }

    private void addListenersForTextFields() {
        this.successRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue.equals(""))
                    return;

                if(Double.parseDouble(newValue) > 1.0)
                {
                    this.successRateText.setText(String.valueOf(1.0));
                    this.successRateSlider.setValue(1.0);
                }
                else if(Double.parseDouble(newValue) < 0.0)
                {
                    this.successRateText.setText(String.valueOf(0.0));
                    this.successRateSlider.setValue(0.0);
                }
                else
                    this.successRateSlider.setValue(Double.parseDouble(newValue));
            } catch(Exception ex)
            {
                ShowPopup("Invalid input in task parameters!", "Invalid input");
                this.successRateText.clear();
            }
        });

        this.successWithWarningRateText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if(newValue.equals(""))
                    return;

                if(Double.parseDouble(newValue) > 1.0)
                {
                    this.successRateWithWarningsSlider.setValue(1.0);
                    this.successWithWarningRateText.setText(String.valueOf(1.0));
                }
                else if(Double.parseDouble(newValue) < 0.0)
                {
                    this.successRateWithWarningsSlider.setValue(0.0);
                    this.successWithWarningRateText.setText(String.valueOf(0.0));
                }
                else
                    this.successRateWithWarningsSlider.setValue(Double.parseDouble(newValue));
            } catch(Exception ex)
            {
                ShowPopup("Invalid input in task parameters!", "Invalid input");
                this.successWithWarningRateText.clear();
            }
        });
    }

    private void addListenersForSliders() {
        this.successRateSlider.valueProperty().addListener((observable, oldValue, newValue) -> this.successRateText.setText(String.format("%.3f", newValue)));
        this.successRateWithWarningsSlider.valueProperty().addListener((observable, oldValue, newValue) -> this.successWithWarningRateText.setText(String.format("%.3f", newValue)));
    }


    private void disableTaskOptions(Boolean flag)
    {
        this.CreateNewTaskButton.setDisable(flag);

        this.currentSelectedTargetLabel.setDisable(flag);
        this.taskSelection.setDisable(flag);
        this.targetSelection.setDisable(flag);
        this.affectedTargets.setDisable(flag);

        this.currentSelectedTargetListView.setDisable(flag);
        this.selectAllButton.setDisable(flag);
        this.deselectAllButton.setDisable(flag);
        this.addSelectedButton.setDisable(flag);

        setForSimulationTask(flag);
    }

    private void addListenersToButtons()
    {
        this.taskTargetDetailsTableView.getItems().addListener(new ListChangeListener<CreateTaskTargetInformation>() {
            @Override
            public void onChanged(Change<? extends CreateTaskTargetInformation> c) {
                AdminCreateTaskController.this.removeSelectedButton.setDisable(c.getList().isEmpty());
                // TaskController.this.clearTableButton.setDisable(c.getList().isEmpty());
            }
        });
    }

    private void addListenersForCompilationButtons() {

        this.taskSelection.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                setVisibilityOfTask(AdminCreateTaskController.this.taskSelection.getValue().equals(AdminCreateTaskController.this.COMPILATION));
            }
        });
    }

    private void setVisibilityOfTask(boolean flag)
    {
        //Compilation
        this.compilationOutputLabel.setVisible(flag);
        this.compilationSourceCodeLabel.setVisible(flag);
        this.toCompileButton.setVisible(flag);
        this.compiledOutputButton.setVisible(flag);
        this.sourceCodePathLabel.setVisible(flag);
        this.outputPathLabel.setVisible(flag);

        //Simulation
        this.processingTimeLabel.setVisible(!flag);
        this.processingTimeTextField.setVisible(!flag);
        this.successRateSlider.setVisible(!flag);
        this.successRateWithWarningsSlider.setVisible(!flag);
        this.limitedPermanentLabel.setVisible(!flag);
        this.limitedRadioButton.setVisible(!flag);
        this.permanentRadioButton.setVisible(!flag);
        this.successWithWarningRateText.setVisible(!flag);
        this.successRateText.setVisible(!flag);
        this.ApplyParametersButton.setVisible(!flag);
        this.successRateLabel.setVisible(!flag);
        this.successRateWithWarnings.setVisible(!flag);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        setAllTargetsList();
        setTaskTargetDetailsTable();
    }

    private void applyTaskParametersForAllTargets(SimulationParameters taskParameters) {
        this.taskParametersMap = new HashMap<>();
        SimulationParameters currTaskParameters;
        Duration processingTime;
        long randomTime;
        double successRate = taskParameters.getSuccessRate(), successRateWithWarnings = taskParameters.getSuccessWithWarnings();
        Boolean isRandom = taskParameters.isRandom();

        //permanent time for all targets
        if(!isRandom)
        {
            for(Target target : this.graph.getGraphTargets().values())
                this.taskParametersMap.put(target.getTargetName(), taskParameters);

            return;
        }

        //Random time for each target
        for(Target target : this.graph.getGraphTargets().values())
        {
            processingTime = taskParameters.getProcessingTime();
            randomTime = (long)(Math.random() * (processingTime.toMillis())) + 1;
            processingTime = (Duration.of(randomTime, ChronoUnit.MILLIS));

            currTaskParameters = new SimulationParameters(processingTime, isRandom, successRate, successRateWithWarnings);
            this.taskParametersMap.put(target.getTargetName(), currTaskParameters);
        }
    }

    private void setAllTargetsList() {
        int i = 0;
        this.allTargetsList = FXCollections.observableArrayList();

        for(Target currentTargetName : this.graph.getGraphTargets().values())
            this.allTargetsList.add(i++, currentTargetName.getTargetName());

        final SortedList<String> sorted = this.allTargetsList.sorted();
        this.targetSelection.getItems().addAll(sorted);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private void ShowPopUp(Alert.AlertType alertType, String title, String header, String message)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}