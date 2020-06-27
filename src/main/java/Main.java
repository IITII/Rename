
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

/**
 * @author IITII
 */
public class Main extends Application {

    final String title = "Rename for Anime";
    final int defaultWidth = 900;
    final int defaultHeight = 750;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/layout/layout_main.fxml"));
        primaryStage.setTitle(title);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icon.png")));
        Scene scene = new Scene(root, defaultWidth, defaultHeight);
        URL resource = getClass().getResource("/stylesheet/stylesheet_main.css");
        scene.getStylesheets().add(String.valueOf(resource));
        primaryStage.setScene(scene);
        //primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}