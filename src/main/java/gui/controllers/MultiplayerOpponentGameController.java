/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 */

package gui.controllers;

import game.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import multiplayer.GameClient;

public class MultiplayerOpponentGameController implements GameUpdateListener {
    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    private Game game;
    private int cellSize;
    private final int boardSize = 5;

    public void setGame(Game opponentGame) {this.game = opponentGame;}

    public void setGameClient(GameClient gameClient) {
        gameClient.setGameUpdateListener(this);
    }

    public void showGame() {
        createGameBoard();
    }

    // Initialize a new game with a ready board and randomly rotated connectors
    private void createGameBoard() {
        int field_size = 250;
        this.cellSize = field_size / boardSize;
        GridHelper.clearGameGrid(gameGrid);
        GridHelper.setupGridConstraints(boardSize, gameGrid, field_size);
        GridHelper.loadImages();
        GridHelper.createCells(game, gameGrid, cellSize, 5, null);
    }

    @Override
    public void onGameUpdate() {
        Platform.runLater(() -> {
            try {
                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        GridHelper.fillCell(game, gameGrid, cellSize, row, col, null, false, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
