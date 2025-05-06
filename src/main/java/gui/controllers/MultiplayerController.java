package gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class MultiplayerController {
    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    @FXML public Button undoButton;
    @FXML public Button redoButton;
    @FXML public Label stepsLabel;
    @FXML public Label timerLabel;

    @FXML public void toMainMenu(ActionEvent actionEvent) {
    }

    @FXML public void getRedo(ActionEvent actionEvent) {
    }

    @FXML public void getUndo(ActionEvent actionEvent) {
    }
}
