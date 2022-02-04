package controllers;

import paths.BodyComponentsPaths;
import http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import paths.Patterns;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class LoginController {
    private Stage primaryStage;
    private PrimaryController primaryController;

    @FXML
    public TextField userNameTextField;

    @FXML
    public Label errorMessageLabel;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.errorMessageLabel.textProperty().bind(this.errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {

        String userName = this.userNameTextField.getText();
        if (userName.isEmpty()) {
            this.errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(Patterns.LOCAL_HOST + Patterns.LOGIN_PATTERN)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        LoginController.this.errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            LoginController.this.errorMessageProperty.set("Login failed: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        try{
                            URL url = getClass().getResource(BodyComponentsPaths.PRIMARY);
                            FXMLLoader fxmlLoader = new FXMLLoader();
                            fxmlLoader.setLocation(url);
                            ScrollPane mainMenuComponent = fxmlLoader.load(Objects.requireNonNull(url).openStream());
                            LoginController.this.primaryController = fxmlLoader.getController();

                            Scene scene = new Scene(mainMenuComponent,1280, 800);
                            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(BodyComponentsPaths.LIGHT_MAIN_THEME)).toExternalForm());
                            LoginController.this.primaryStage.setTitle("G.P.U.P");
                            LoginController.this.primaryController.initialize(LoginController.this.primaryStage, response.header("username"));
                            LoginController.this.primaryStage.setScene(scene);
                        }
                        catch(Exception ignore) {}
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        this.errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }
}
