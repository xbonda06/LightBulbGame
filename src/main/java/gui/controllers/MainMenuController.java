package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleEasy() throws IOException {
        startGame(5);
    }

    @FXML
    private void handleMedium() throws IOException {
        startGame(7);
    }

    @FXML
    private void handleHard() throws IOException {
        startGame(9);
    }

    private void startGame(int size) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game_board.fxml"));
        Parent root = loader.load();

        GameBoardController controller = loader.getController();
        controller.setBoardSize(size);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - " + size + "x" + size);
    }
}