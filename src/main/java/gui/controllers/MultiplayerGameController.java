package gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import multiplayer.GameClient;
import multiplayer.GameServer;

public class MultiplayerGameController {
    private Stage primaryStage;
    private GameServer server;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public void setServer(GameServer server) {this.server = server;}

    @FXML
    public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    @FXML public Button undoButton;
    @FXML public Button redoButton;
    @FXML public Label stepsLabel;
    @FXML public Label timerLabel;

    @FXML public void getRedo() {
    }

    @FXML public void getUndo() {
    }

    public void toTheMain() {
        server.stop();
        GridHelper.loadMainMenu(primaryStage);
    }
}
