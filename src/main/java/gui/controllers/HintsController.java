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
        showHints();
    }

    public void reloadHints(Game game, int r, int c) {
        this.game = game;
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                boolean animate = (r == row && c == col); //for smooth rotation
                GridHelper.fillCell(game, gameGrid, cellSize, imageCache, row, col, null, animate);
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
        System.out.println("Hint at (" + row + "," + col + "): " + hints);
        img = switch (hints) {
            case 1 -> imageCache.get("hint1");
            case 2 -> imageCache.get("hint2");
            case 3 -> imageCache.get("hint3");
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
