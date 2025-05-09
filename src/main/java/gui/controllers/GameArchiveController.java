/**
 * Controller responsible for displaying a previously played game in archive mode.
 * <p>
 * The game is shown in a read-only format with limited interaction: only undo/redo steps are allowed.
 * Users can view the board, inspect the game's progress, and optionally restart the game from its archived state.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */

package gui.controllers;

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


    /**
     * Sets the reference to the primary application stage.
     *
     * @param primaryStage the main stage of the application
     */
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    /**
     * Loads the archived game based on the given game ID.
     * <p>
     * Initializes the game, board size, and cell size. It then populates the game grid
     * with the archived game's state.
     * </p>
     *
     * @param gameId the ID of the archived game to load
     */
    public void loadGame(int gameId) {
        gameIdLabel.setText("Archive - Game " + gameId);
        GridHelper.loadImages();

        GameDeserializer deserializer = GameArchive.load(gameId);
        this.game = deserializer.getGame();
        this.boardSize = game.rows();
        this.cellSize = 400 / boardSize;

        GridHelper.createCells(game, gameGrid, cellSize, boardSize, null);
    }


    /**
     * Updates the displayed steps count.
     * <p>
     * This method updates the `stepsLabel` to reflect the current number of steps taken
     * during the game, as tracked by the undo/redo actions.
     * </p>
     */
    private void updateStepsDisplay() {
        stepsLabel.setText(String.format("Steps: %d", stepsTaken));
    }

    /**
     * Handles the redo action, restoring the game state to a previous step.
     * <p>
     * If a redo is possible, it is performed, and the steps counter is decreased.
     * </p>
     */
    @FXML
    public void getRedo() {
        if (GridHelper.redo(this.game, this.boardSize, this.gameGrid, this.cellSize, null, true)){
            --stepsTaken;
            updateStepsDisplay();
        }
    }


    /**
     * Handles the undo action, reverting the game state to a prior step.
     * <p>
     * If an undo is possible, it is performed, and the steps counter is increased.
     * </p>
     */
    @FXML
    public void getUndo() {
        if(GridHelper.undo(this.game, this.boardSize, this.gameGrid, this.cellSize, null, true)){
            ++stepsTaken;
            updateStepsDisplay();
        }
    }

    /**
     * Starts a new game by loading the game board in normal play mode.
     * <p>
     * This method clears the archived game's history and initializes a new game
     * session with the same board size as the archived game.
     * </p>
     *
     * @throws IOException if the FXML file for the new game board cannot be loaded
     */
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

    /**
     * Navigates back to the game archive view.
     * <p>
     * This method reloads the archive screen, allowing the user to browse other archived games.
     * </p>
     *
     * @throws IOException if the archive view FXML file cannot be loaded
     */
    @FXML public void toArchive() throws IOException {
        GridHelper.loadArchive(primaryStage);
    }
}
