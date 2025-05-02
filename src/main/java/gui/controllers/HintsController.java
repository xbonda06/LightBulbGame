package gui.controllers;

import common.GameNode;
import common.Position;
import game.Game;
import javafx.fxml.FXML;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HintsController {

    @FXML
    public GridPane gameGrid;

    private Game game;
    private final int FIELD_SIZE = 400;

    public void init(Game game) {
        this.game = game;
    }

    public void reloadHints(Game game) {
        this.game = game;
        int size = game.rows();
        int cellSize = FIELD_SIZE / size;

        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();

        for (int i = 0; i < size; i++) {
            gameGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
            gameGrid.getRowConstraints().add(new RowConstraints(cellSize));
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                GameNode node = game.node(new Position(row + 1, col + 1));

                Rectangle cell = new Rectangle(cellSize - 2, cellSize - 2);
                cell.setFill(Color.web("#1D1033"));
                cell.setStroke(Color.BLACK);
                gameGrid.add(cell, col, row);
            }
        }
    }

    @FXML
    public void closeHints() {
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
