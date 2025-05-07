package gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {
    //UI components
    @FXML public Button easyButton;
    @FXML public Button mediumButton;
    @FXML public Button hardButton;
    @FXML public Button multiplayerButton;
    @FXML public Button archiveButton;

    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

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

    private void startGame(int size) throws IOException {
        GridHelper.startGame(size, primaryStage);
    }

    @FXML public void openArchive() throws IOException {
        GridHelper.loadArchive(primaryStage);
    }

    @FXML public void startMultiplayer() throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/multiplayer_menu.fxml"));
        Parent root = loader.load();
        MultiplayerController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
    }
}