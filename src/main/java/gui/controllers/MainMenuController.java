package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the main menu of the game.
 * <p>
 * This controller handles user interactions on the main menu screen, including:
 * selecting a difficulty level for a new game, navigating to multiplayer mode,
 * and accessing the archive of previously played games.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MainMenuController {
    //UI components
    @FXML public Button easyButton;
    @FXML public Button mediumButton;
    @FXML public Button hardButton;
    @FXML public Button multiplayerButton;
    @FXML public Button archiveButton;

    private Stage primaryStage;
    /**
     * Sets the primary stage reference for this controller.
     *
     * @param primaryStage the main window of the application
     */
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    /**
     * Handles clicks on difficulty buttons and starts a game with the selected size.
     *
     * @param event the button click event
     * @throws IOException if loading the game scene fails
     */
    @FXML
    private void handleDifficulty(javafx.event.ActionEvent event) throws IOException {
        String buttonId = ((javafx.scene.control.Button) event.getSource()).getId();
        int size = switch (buttonId) {
            case "easyButton" -> 5;
            case "mediumButton" -> 7;
            case "hardButton" -> 9;
            default -> throw new IllegalArgumentException("Unknown difficulty level: " + buttonId);
        };
        startGame(size);
    }

    /**
     * Starts the game with the specified board size.
     *
     * @param size the size of the board (e.g., 5 for easy)
     * @throws IOException if the game scene cannot be loaded
     */
    private void startGame(int size) throws IOException {
        GridHelper.startGame(size, primaryStage);
    }

    /**
     * Opens the archive scene where saved games can be reviewed.
     *
     * @throws IOException if the archive scene fails to load
     */
    @FXML public void openArchive() throws IOException {
        GridHelper.loadArchive(primaryStage);
    }

    /**
     * Loads and displays the multiplayer menu scene.
     *
     * @throws IOException if the multiplayer menu fails to load
     */
    @FXML public void startMultiplayer() throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/multiplayer_menu.fxml"));
        Parent root = loader.load();
        MultiplayerController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
    }
}