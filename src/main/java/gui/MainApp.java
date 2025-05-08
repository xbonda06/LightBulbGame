/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * This is the main entry point for the Light Bulb Game application. The application
 * uses JavaFX for the user interface and loads the main menu scene from an FXML file.
 * The MainApp class extends the Application class and overrides the `start` method to
 * set up the primary stage, load the FXML file for the main menu, and configure the scene
 * with the appropriate controller.
 */

package gui;

import gui.controllers.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
        Parent root = loader.load();

        MainMenuController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Light Bulb Game");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}