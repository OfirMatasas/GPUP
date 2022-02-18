package controllers;

import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import paths.BodyComponentsPaths;
import patterns.Patterns;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class WorkerLoginController {
    //------------------------------------------------- Members ----------------------------------------------------//
    private Stage primaryStage;
    private WorkerPrimaryController workerPrimaryController;
    private String username;
    private Integer numOfThreads;
    //----------------------------------------------- FXML Members -------------------------------------------------//
    @FXML private Button loginButton;
    @FXML private Spinner ThreadSpinner;
    @FXML private TextField userNameTextField;
    @FXML private Label errorMessageLabel;

    //------------------------------------------------- Settings ----------------------------------------------------//
    @FXML public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeThreadSpinner();
    }

    private void initializeThreadSpinner() {
        this.ThreadSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
    }

    //------------------------------------------------ Logging in ---------------------------------------------------//
    @FXML public void userNameTextFieldKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode()== KeyCode.ENTER)
            loginButtonClicked(new ActionEvent());
    }

    @FXML private void loginButtonClicked(ActionEvent event) {

        String userName = this.userNameTextField.getText();
        if (userName.isEmpty()) {
            loginError("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(Patterns.LOGIN)
                .newBuilder()
                .addQueryParameter("workerUsername", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> loginError("Something went wrong: " + e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() -> {
                        String username = response.header("username");
                        loggedInAsWorker(username);
                    });
                else //Failure
                {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    Platform.runLater(() -> loginError("Login failed: " + responseBody));
                }
                Objects.requireNonNull(response.body()).close();
            }
        });
    }

    private void loggedInAsWorker(String username) {
        try{
            this.username = username;
            this.numOfThreads = Integer.parseInt(this.ThreadSpinner.getValue().toString());

            URL url = getClass().getResource(BodyComponentsPaths.PRIMARY);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            ScrollPane mainMenuComponent = fxmlLoader.load(Objects.requireNonNull(url).openStream());
            WorkerLoginController.this.workerPrimaryController = fxmlLoader.getController();

            Scene scene = new Scene(mainMenuComponent,1280, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());
            WorkerLoginController.this.primaryStage.setTitle("G.P.U.P");
            WorkerLoginController.this.primaryStage.setScene(scene);
            WorkerLoginController.this.workerPrimaryController.initialize(this.primaryStage, this.username, this.numOfThreads);
        }
        catch (Exception e) { System.out.println("Error uploading app: " + e.getMessage()); }
    }

    private void loginError(String errorMessage) {
        this.errorMessageLabel.setText(errorMessage);
    }

    @FXML private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }

    public String getCurrentUser() {
        return this.username;
    }
}