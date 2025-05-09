/**
 * Controller for displaying the opponent's game board in a multiplayer match.
 * <p>
 * This class manages the UI for one of the opponent's boards, initializes the game grid,
 * and updates the display in real-time based on received game updates.
 * </p>
 *
 * <p>
 * Used in the multiplayer mode to show the current state of other players’ boards
 * without allowing interaction.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
package gui.controllers;

import game.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import multiplayer.GameClient;

/**
 * Handles the display and live update of a remote player's board in a multiplayer game session.
 * Implements {@link GameUpdateListener} to respond to board changes.
 */
public class MultiplayerOpponentGameController implements GameUpdateListener {
    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    private Game game;
    private int cellSize;
    private final int boardSize = 5;

    /**
     * Sets the game state for the opponent's board.
     *
     * @param opponentGame the {@link Game} instance representing the opponent's game
     */
    public void setGame(Game opponentGame) {this.game = opponentGame;}


    /**
     * Registers this controller to listen for updates from the specified game client.
     *
     * @param gameClient the {@link GameClient} that provides game state updates
     */
    public void setGameClient(GameClient gameClient) {
        gameClient.setGameUpdateListener(this);
    }

    /**
     * Initializes and displays the opponent's game board.
     */
    public void showGame() {
        createGameBoard();
    }

    /**
     * Creates the opponent's game board UI using static data.
     * Initializes the grid layout, loads images, and fills in the cells.
     */
    private void createGameBoard() {
        int field_size = 250;
        this.cellSize = field_size / boardSize;
        GridHelper.clearGameGrid(gameGrid);
        GridHelper.setupGridConstraints(boardSize, gameGrid, field_size);
        GridHelper.loadImages();
        GridHelper.createCells(game, gameGrid, cellSize, 5, null);
    }

    /**
     * Callback for game state updates received from the server.
     * Re-renders the opponent's game board on the JavaFX Application Thread.
     */
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
