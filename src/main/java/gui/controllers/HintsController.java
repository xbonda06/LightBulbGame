package gui.controllers;

import game.Game;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Map;

public class HintsController {

    @FXML
    public GridPane gameGrid;

    private Game game;
    private int cellSize;
    private int boardSize;
    private Map<String, Image> imageCache;

    public void init(Game game, int cellSize, int boardSize, Map<String, Image> imageCache) {
        this.game = game;
        this.cellSize = cellSize;
        this.imageCache = imageCache;
        this.boardSize = boardSize;
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, null, imageCache );
    }

    public void reloadHints(Game game) {
        this.game = game;
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                GridHelper.fillCell(game, gameGrid, cellSize, imageCache, row, col, null);
            }
        }
    }

    @FXML
    public void closeHints() {
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
