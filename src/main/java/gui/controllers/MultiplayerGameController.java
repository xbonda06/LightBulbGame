package gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import multiplayer.GameClient;

public class MultiplayerGameController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

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
        GridHelper.loadMainMenu(primaryStage);
    }
}
