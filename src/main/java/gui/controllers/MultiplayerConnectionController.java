package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameServer;

public class MultiplayerConnectionController {
    @FXML public Button startButton;
    @FXML public Label ipAddress;
    @FXML public Label playerCount;
    @FXML public Label port;
    private Stage primaryStage;
    private GameServer server;

    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public void setServer(GameServer server) {this.server = server;}

    @FXML public void toTheMain() {
        server.stop();
        GridHelper.loadMainMenu(primaryStage);
    }

    public void startGame() {

    }
}
