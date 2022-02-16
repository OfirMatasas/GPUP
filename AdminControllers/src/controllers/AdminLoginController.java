package controllers;

import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

public class AdminLoginController {
    public Button loginButton;
    private Stage primaryStage;
    private AdminPrimaryController primaryController;
    private String username;
    @FXML public TextField userNameTextField;
    @FXML public Label errorMessageLabel;

    @FXML public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
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
                .addQueryParameter("adminUsername", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> loginError("Something went wrong: " + e.getMessage()));
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() >= 200 && response.code() < 300) //Success
                    Platform.runLater(() -> loggedInAsAdmin(response));
                else //Failure
                {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    Platform.runLater(() -> loginError("Login failed: " + responseBody));
                }
            }
        });
    }

    private void loggedInAsAdmin(Response response) {
        try{
            AdminLoginController.this.username = response.header("username");

            URL url = getClass().getResource(BodyComponentsPaths.PRIMARY);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            ScrollPane mainMenuComponent = fxmlLoader.load(Objects.requireNonNull(url).openStream());
            AdminLoginController.this.primaryController = fxmlLoader.getController();

            Scene scene = new Scene(mainMenuComponent,1280, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());
            AdminLoginController.this.primaryStage.setTitle("G.P.U.P");
            AdminLoginController.this.primaryController.initialize(AdminLoginController.this.primaryStage, response.header("username"));
            AdminLoginController.this.primaryStage.setScene(scene);
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

    @FXML public void userNameTextFieldKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode()== KeyCode.ENTER)
            loginButtonClicked(new ActionEvent());
    }
}
