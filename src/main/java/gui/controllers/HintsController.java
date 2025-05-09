/**
 * Controller responsible for managing the hints window UI.
 * <p>
 * It displays a read-only version of the current game board and overlays hint icons
 * indicating the suggested number of rotations for each cell.
 * The hints are updated dynamically during gameplay to guide the user.
 * </p>
 *
 * Features:
 * <ul>
 *     <li>Initializes the board based on the current game state.</li>
 *     <li>Redraws and updates hints as the game progresses.</li>
 *     <li>Allows the user to close the hints window programmatically.</li>
 * </ul>
 *
 * @author Olha Tomylko (xtomylo00)
 */

package gui.controllers;

import common.GameNode;
import common.Position;
import game.Game;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HintsController {

    @FXML
    public GridPane gameGrid;

    private Game game;
    private int cellSize;
    private int boardSize;

    /**
     * Initializes the hints window with the current game state.
     * <p>
     * This method sets up the board, creating the necessary cells and displaying the hint icons
     * based on the current state of the game.
     * </p>
     *
     * @param game The current game instance.
     * @param cellSize The size of each cell in the grid.
     * @param boardSize The size of the game board (number of rows/columns).
     */
    public void init(Game game, int cellSize, int boardSize) {
        this.game = game;
        this.cellSize = cellSize;
        this.boardSize = boardSize;
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, null);
        showHints();
    }


    /**
     * Reloads the hints window and updates the hint icons after a specific move.
     * <p>
     * This method updates the board and the hint icons based on the new game state after the
     * specified row and column are updated.
     * </p>
     *
     * @param game The updated game instance.
     * @param r The row index of the updated cell.
     * @param c The column index of the updated cell.
     */
    public void reloadHints(Game game, int r, int c) {
        this.game = game;
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                boolean animate = (r == row && c == col); //for smooth rotation
                GridHelper.fillCell(game, gameGrid, cellSize, row, col, null, animate, false);
                updateHint(row, col);
            }
        }
    }


    /**
     * Displays the hint icons for all cells in the game board.
     * <p>
     * This method iterates through all cells and updates the hint icons based on the current game state.
     * </p>
     */
    private void showHints(){
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                updateHint(row, col);
            }
        }
    }

    /**
     * Updates the hint icon for a specific cell based on the number of rotations required.
     * <p>
     * This method assigns the correct hint icon to a given cell based on the number of rotations needed.
     * </p>
     *
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     */
    private void updateHint(int row, int col){
        GameNode node = game.node(new Position(row + 1, col + 1));
        Image img;
        int hints = node.getHint();
        img = switch (hints) {
            case 1 -> GridHelper.getImage("hint1");
            case 2 -> GridHelper.getImage("hint2");
            case 3 -> GridHelper.getImage("hint3");
            default -> null;
        };

        if (img != null) {
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(cellSize);
            imageView.setFitHeight(cellSize);
            imageView.setPreserveRatio(false);
            gameGrid.add(imageView, col, row);
        }
    }

    /**
     * Closes the hints window.
     * <p>
     * This method triggers the window close event to close the hints window programmatically.
     * </p>
     */
    @FXML
    public void closeHints() {
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
