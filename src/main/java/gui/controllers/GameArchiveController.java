package gui.controllers;

import common.GameNode;
import game.Game;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import json.GameArchive;
import json.GameDeserializer;

import java.io.IOException;

public class GameArchiveController {
    @FXML public GridPane gameGrid;
    @FXML public Button undoButton;
    @FXML public Button redoButton;
    @FXML public StackPane rootPane;
    @FXML public Label gameIdLabel;
    @FXML public Label stepsLabel;

    private int stepsTaken = 0;

    private int boardSize = 0;
    private Game game;
    private Stage primaryStage;
    private int cellSize;

    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public void loadGame(int gameId) {
        gameIdLabel.setText("Archive - Game " + gameId);
        GridHelper.loadImages();

        GameDeserializer deserializer = GameArchive.load(gameId);
        this.game = deserializer.getGame();
        this.boardSize = game.rows();
        this.cellSize = 400 / boardSize;

        GridHelper.createCells(game, gameGrid, cellSize, boardSize, null);
    }

    private void updateStepsDisplay() {
        stepsLabel.setText(String.format("Steps: %d/25", stepsTaken));
    }

    @FXML
    public void getRedo() {
        if (GridHelper.redo(this.game, this.boardSize, this.gameGrid, this.cellSize, null, true)){
            --stepsTaken;
            updateStepsDisplay();
        }
    }

    @FXML
    public void getUndo() {
        if(GridHelper.undo(this.game, this.boardSize, this.gameGrid, this.cellSize, null, true)){
            ++stepsTaken;
            updateStepsDisplay();
        }
    }

    @FXML public void startGame() throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/game_board.fxml"));
        Parent root = loader.load();

        GameBoardController controller = loader.getController();

        controller.setFromArchive(true);
        game.clearHistory();
        controller.setGame(game);

        controller.setBoardSize(boardSize);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - " + boardSize + "x" + boardSize);
    }

    @FXML public void toArchive() throws IOException {
        GridHelper.loadArchive(primaryStage);
    }
}
