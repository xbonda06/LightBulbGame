/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 */

package gui.controllers;

import game.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import multiplayer.GameClient;

public class MultiplayerOpponentGameController implements GameUpdateListener {
    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    private Game game;
    private int cellSize;
    private GameClient client;

    public void setGame(Game opponentGame) {this.game = opponentGame;}

    public void setGameClient(GameClient gameClient) {
        this.client = gameClient;
        this.client.setGameUpdateListener(this);
    }

    public void showGame() {
        createGameBoard();
    }

    // Initialize a new game with a ready board and randomly rotated connectors
    private void createGameBoard() {
        this.cellSize = 250 / 5;
        clearGameGrid();
        setupGridConstraints();
        GridHelper.loadImages();
        GridHelper.createCells(game, gameGrid, cellSize, 5, null);
    }

    @Override
    public void onGameUpdate() {
        Platform.runLater(() -> {
            try {
                for (int row = 0; row < 5; row++) {
                    for (int col = 0; col < 5; col++) {
                        GridHelper.fillCell(game, gameGrid, cellSize, row, col, null, false, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Delete game
    private void clearGameGrid() {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();
    }

    // Configure grid size and set fixed cell dimensions based on the board size
    private void setupGridConstraints() {
        int cellSize = 250 / 5;
        gameGrid.setMinSize(250, 250);
        gameGrid.setPrefSize(250, 250);
        gameGrid.setMaxSize(250, 250);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints colConst = new ColumnConstraints(cellSize, cellSize, cellSize);
            RowConstraints rowConst = new RowConstraints(cellSize, cellSize, cellSize);
            gameGrid.getColumnConstraints().add(colConst);
            gameGrid.getRowConstraints().add(rowConst);
        }
    }
}
