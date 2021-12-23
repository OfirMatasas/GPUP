package userInterface;

import com.sun.media.jfxmedia.events.PlayerEvent;
import controllers.PrimaryController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.*;

import java.util.Objects;
import java.util.Optional;

public class Main extends Application
{
    Boolean confirmExit = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("G.P.U.P");

        Parent load = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("PrimaryScene.fxml")));
        Scene scene = new Scene(load, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit dialog");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to exit?");
                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");

                alert.getButtonTypes().setAll(yesButton, noButton );
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != yesButton){
                    event.consume();
                }
            }
        });
    }

    public static void main(String[] args)
    {
        launch(Main.class);
    }
}