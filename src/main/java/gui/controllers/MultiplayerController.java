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
    @FXML public Button createButton;
    @FXML public Button joinGame;
    @FXML public Button mainButton;
    @FXML public Button startButton;
    @FXML public Label ipAddress;
    @FXML public Label playerCount;

    // From the game mode -> multiplayer_main.fxml

    @FXML public void toMainMenu() {
    }

    @FXML public void getRedo() {
    }

    @FXML public void getUndo() {
    }

    // From the menu when choosing CREATE or JOIN -> multiplayer_menu.fxml

    @FXML public void createGame() {
    }

    @FXML public void joinGame() {
    }

    @FXML public void toTheMain() {
    }

    // From waiting screen -> wait_for_connection.fxml

    @FXML public void startGame() {
    }
}

//NEW GAME:
//instance Server. Port random
//instance client + connect

//JOIN GAME:
//IP and Port
//instance client

