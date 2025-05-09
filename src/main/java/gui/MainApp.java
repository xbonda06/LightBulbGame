package gui;

import gui.controllers.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Light Bulb Game application.
 * <p>
 * This application uses JavaFX for its user interface. The main menu scene is loaded
 * from an FXML file, and the primary stage is set up to display this menu.
 * The application is launched by overriding the {@link #start(Stage)} method,
 * which initializes the main menu and sets up the necessary controller.
 * </p>
 *
 * <p>
 * The {@link #main(String[])} method serves as the application's entry point and
 * launches the JavaFX application.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MainApp extends Application {
    /**
     * Initializes and displays the primary stage with the main menu scene.
     * <p>
     * The FXML file for the main menu is loaded, and the controller is configured.
     * The scene is set on the primary stage, which is displayed to the user.
     * </p>
     *
     * @param primaryStage the primary stage to be used for the application window
     * @throws Exception if loading the FXML file or setting up the scene fails
     */
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

    /**
     * The main method for launching the application.
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}