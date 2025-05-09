/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * This controller manages the hints window for the puzzle game.
 * It displays a read-only version of the current game board and overlays hint icons
 * indicating the suggested number of rotations for each cell.
 *
 * Features:
 * - Initializes the board based on the current game state.
 * - Updates and redraws hints dynamically during gameplay.
 * - Provides a method to close the hints window programmatically.
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

    public void init(Game game, int cellSize, int boardSize) {
        this.game = game;
        this.cellSize = cellSize;
        this.boardSize = boardSize;
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, null);
        showHints();
    }

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

    private void showHints(){
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                updateHint(row, col);
            }
        }
    }

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

    @FXML
    public void closeHints() {
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
