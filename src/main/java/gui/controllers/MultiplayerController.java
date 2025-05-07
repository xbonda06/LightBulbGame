package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MultiplayerController {
    public Label port;
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

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

    @FXML public void createGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/wait_for_connection.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
    }

    @FXML public void joinGame() {
        GridHelper.loadDialog(rootPane, primaryStage, "/fxml/join_dialog.fxml");
    }

    @FXML public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
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

