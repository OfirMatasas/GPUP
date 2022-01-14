package userInterface;

import bodyComponentsPaths.BodyComponentsPaths;
import controllers.PrimaryController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("G.P.U.P");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(BodyComponentsPaths.PRIMARY));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 1000,700));
        primaryStage.getScene().getStylesheets().add(BodyComponentsPaths.LIGHT_MAIN_THEME);
        primaryStage.getIcons().add(new Image("/resourcers/GPUP logo.png"));

        //Set the Stage
        PrimaryController primaryController = loader.getController();
        primaryController.setPrimaryStage(primaryStage);
        //show the stage
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(Main.class);
    }
}