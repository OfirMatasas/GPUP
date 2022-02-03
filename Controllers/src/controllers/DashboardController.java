package controllers;

import dtos.DashboardGraphDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DashboardController {

    private final Map<String, DashboardGraphDTO> graphsMap = new HashMap<>();

    @FXML
    private VBox DashboardVBox;

    @FXML
    private ListView<String> GraphsListView;

    @FXML
    private TextArea GraphDetailsTextArea;

    @FXML
    private Button SelectGraphButton;

    @FXML
    public void GraphSelectedFromListView(MouseEvent mouseEvent) {
        String selectedGraphName = this.GraphsListView.getSelectionModel().getSelectedItem();
        DashboardGraphDTO selectedGraph;

        if(selectedGraphName == null)
            return;

        selectedGraph = this.graphsMap.get(selectedGraphName);

        this.GraphDetailsTextArea.clear();
        this.GraphDetailsTextArea.appendText("Graph name: " + selectedGraphName + "\n");
        this.GraphDetailsTextArea.appendText("Graph uploader: " + selectedGraph.getUploaderName() + "\n\n");

        this.GraphDetailsTextArea.appendText("Number of targets: " + selectedGraph.getTargets().size() + "\n");
        this.GraphDetailsTextArea.appendText("- Roots: " + selectedGraph.getTargets().get("roots") + "\n");
        this.GraphDetailsTextArea.appendText("- Middles: " + selectedGraph.getTargets().get("middles") + "\n");
        this.GraphDetailsTextArea.appendText("- Leaves: " + selectedGraph.getTargets().get("leaves") + "\n");
        this.GraphDetailsTextArea.appendText("- Independents: " + selectedGraph.getTargets().get("independents") + "\n\n");

        this.GraphDetailsTextArea.appendText("Tasks prices: \n");
        for(String taskName : selectedGraph.getTasksPrices().keySet())
            this.GraphDetailsTextArea.appendText("- " + taskName +": " + selectedGraph.getTasksPrices().get("taskName") + "\n");
    }

//    @FXML
//    public void GraphSelectedButtonPressed(ActionEvent actionEvent) {
//
//        //noinspection ConstantConditions
//        String finalUrl = HttpUrl
//                .parse(Patterns.LOCAL_HOST + Patterns.LOGIN_PATTERN)
//                .newBuilder()
//                .addQueryParameter("graphname", this.GraphsListView.getSelectionModel().getSelectedItem())
//                .build()
//                .toString();
//
//
//        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
//
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                Platform.runLater(() -> ShowPopUp("Failed to connect to server", "Connection Failure", Alert.AlertType.ERROR)
//                );
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                if (response.code() != HttpServletResponse.SC_ACCEPTED) //Failure
//                {
//                    String responseBody = Objects.requireNonNull(response.body()).string();
//                    Platform.runLater(() ->
//                            ShowPopUp(responseBody, "Loading Graph Failed", Alert.AlertType.ERROR)
//                    );
//                }
//                else  //Success
//                {
//                    Platform.runLater(() -> {
//                        try{
//                            PrimaryController primaryController = new PrimaryController();
//
//                            File file = convertResponseBodyToTempFile(response);
//
//                            primaryController.loadGraph(file);
//
//                            if (file != null) {
//                                file.deleteOnExit();
//                            }
//                        } catch(Exception ignore) {}
//                    });
//                }
//            }
//        });
//    }

    private File convertResponseBodyToTempFile(Response response) {
        try {
            File tempFile = File.createTempFile("tempgraph", ".xml");

            FileWriter fileWriter = new FileWriter(tempFile, true);
            fileWriter.write(Objects.requireNonNull(response.body()).string());

            return tempFile;

        } catch (IOException e) {
            ShowPopUp("Error in loading graph file", "loading failed", Alert.AlertType.ERROR);
        }

        return null;
    }

    private void ShowPopUp(String message, String title, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void AddGraphToListView(String graphName)
    {
        if(!this.GraphsListView.getItems().contains(graphName))
            this.GraphsListView.getItems().add(graphName);
    }
}
