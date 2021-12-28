package userInterface;

import bodyComponentsPaths.BodyComponentsPaths;
import controllers.PrimaryController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("G.P.U.P");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PrimaryScene.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.getScene().getStylesheets().add(BodyComponentsPaths.LIGHT_MAIN_THEME);
        primaryStage.getIcons().add(new Image("/resourcers/GPUP logo.png"));

        //Set the Stage
        PrimaryController primaryController = loader.getController();
        primaryController.setPrimaryStage(primaryStage);
        //show the stage
        primaryStage.show();

//        Parent load = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("PrimaryScene.fxml")));
//        Scene scene = new Scene(load, 1000, 600);
//        scene.getStylesheets().addAll(getClass().getResource("Stylesheets/DarkMode.css").toExternalForm());
//        primaryStage.setScene(scene);


//        PrimaryController newProjectController = new FXMLLoader(getClass().getResource("NewProject.fxml")).getController();
//        newProjectController.setStage(stage);
//
//        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit dialog");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to exit?");
            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");

            alert.getButtonTypes().setAll(yesButton, noButton);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != yesButton){
                event.consume();
            }
        });
    }

    public static void main(String[] args)
    {
        launch(Main.class);
    }
}