package userInterface;

import paths.BodyComponentsPaths;
import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("G.P.U.P Login");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(BodyComponentsPaths.LOGIN));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 300,200));
        primaryStage.getScene().getStylesheets().add(BodyComponentsPaths.LOGIN_THEME);

        //Set the Stage
        LoginController loginController = loader.getController();
        loginController.initialize(primaryStage);
        //show the stage
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(Main.class);
    }
}