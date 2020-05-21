package quixxapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Hello World!");

        Pane pane = new Pane(); 
        pane.getChildren().add(new Label("Hello World!"));
        primaryStage.setScene(new Scene(pane));
        primaryStage.setTitle("Quixx App");
        primaryStage.show();
    }
}
