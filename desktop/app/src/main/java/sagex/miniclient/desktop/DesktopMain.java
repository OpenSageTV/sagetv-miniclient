package sagex.miniclient.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DesktopMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("DesktopMain.fxml"));

        Scene scene = new Scene(root, 300, 275);

        stage.setTitle("SageTV Desktop MiniClient");
        stage.setScene(scene);
        stage.show();
    }
}