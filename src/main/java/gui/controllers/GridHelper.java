package gui.controllers;

import common.GameNode;
import common.Position;
import game.Game;
import javafx.animation.RotateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class GridHelper {
    public static void createCells(Game game, GridPane grid, int cellSize, int boardSize, Consumer<GameNode> clickHandler, Map<String, Image> imageCache) {

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                GameNode node = game.node(new Position(row + 1, col + 1));
                Rectangle cell = new Rectangle(cellSize - 2, cellSize - 2);
                cell.setFill(Color.web("#1D1033"));
                cell.setStroke(Color.BLACK);
                if (clickHandler != null)
                    cell.setOnMouseClicked(event -> clickHandler.accept(node));

                grid.add(cell, col, row);
                fillCell(game, grid, cellSize, imageCache, row, col, clickHandler, false, false);
            }
        }
    }

    public static void fillCell(Game game, GridPane grid, int cellSize, Map<String, Image> imageCache, int row, int col, Consumer<GameNode> clickHandler, boolean animate, boolean isUndo) {
        GameNode node = game.node(new Position(row + 1, col + 1));
        double rotationAngle = 0;
        ImageView connectorView = null;
        Image img = null;

        if (node.isPower()) {
            int count = 0;
            if (node.north()) count++;
            if (node.south()) count++;
            if (node.east()) count++;
            if (node.west()) count++;

            if (count == 4) {
                img = imageCache.get("power_4");
            } else if (count == 3) {
                img = imageCache.get("power_3");
                if (!node.north()) rotationAngle = 180;
                else if (!node.south()) rotationAngle = 0;
                else if (!node.east()) rotationAngle = 270;
                else rotationAngle = 90;
            } else if (count == 1) {
                img = imageCache.get("power_1");
                if (node.north()) rotationAngle = 0;
                else if (node.south()) rotationAngle = 180;
                else if (node.east()) rotationAngle = 90;
                else rotationAngle = 270;
            } else if ((node.north() && node.south()) || (node.east() && node.west())) {
                img = imageCache.get("power_2ud");
                rotationAngle = node.north() ? 0 : 90;
            } else if ((node.north() || node.south()) && (node.east() || node.west())) {
                img = imageCache.get("power_2");
                if (node.north() && node.east()) rotationAngle = 0;
                else if (node.north() && node.west()) rotationAngle = 270;
                else if (node.south() && node.east()) rotationAngle = 90;
                else rotationAngle = 180;
            }
        } else if (node.isBulb()) {
            if (node.north()) {
                rotationAngle = 0;
            } else if (node.south()) {
                rotationAngle = 180;
            } else if (node.east()) {
                rotationAngle = 90;
            } else if (node.west()) {
                rotationAngle = 270;
            }
            if (!node.light()){
                connectorView = new ImageView(imageCache.get("bulb_off"));
                img = imageCache.get("short_off");
            }
            else {
                connectorView = new ImageView(imageCache.get("short_on"));
                img = imageCache.get("bulb_on");
                connectorView.setRotate(rotationAngle);
                rotationAngle = 0;
            }
            connectorView.setFitWidth(cellSize);
            connectorView.setFitHeight(cellSize);
            connectorView.setPreserveRatio(false);
        } else {
            if (node.isCross()) {
                img = node.light() ? imageCache.get("cross_on") : imageCache.get("cross_off");
            } else if (node.isHalfCross()) {
                img = node.light() ? imageCache.get("half_cross_on") : imageCache.get("half_cross_off");
                if (node.north() && node.south() && node.east()) rotationAngle = 0;
                else if (node.north() && node.south() && node.west()) rotationAngle = 180;
                else if (node.east() && node.west() && node.north()) rotationAngle = 270;
                else if (node.east() && node.west() && node.south()) rotationAngle = 90;
            } else if (node.isCorner()) {
                img = node.light() ? imageCache.get("corner_on") : imageCache.get("corner_off");
                if (node.north() && node.east()) rotationAngle = 0;
                else if (node.east() && node.south()) rotationAngle = 90;
                else if (node.south() && node.west()) rotationAngle = 180;
                else if (node.west() && node.north()) rotationAngle = 270;
            } else if (node.isLong()) {
                img = node.light() ? imageCache.get("long_on") : imageCache.get("long_off");
                rotationAngle = node.east() && node.west() ? 90 : 0;
            } else {
                img = imageCache.get("short_off");
            }
        }

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(cellSize);
        imageView.setFitHeight(cellSize);

        // if this cell was clicked, smooth rotation
        if (animate && !(node.isBulb() && node.light())) {
            double previousRotation = isUndo ? (rotationAngle + 90 + 360) % 360 : (rotationAngle - 90) % 360;
            RotateTransition rotate = new RotateTransition(Duration.millis(220), imageView);
            rotate.setFromAngle(previousRotation);
            double rotation = isUndo ? -90 : 90;
            rotate.setByAngle(rotation);
            rotate.setCycleCount(1);
            rotate.setAutoReverse(false);
            rotate.play();
        } else {
            imageView.setRotate(rotationAngle);
        }

        imageView.setPreserveRatio(false);
        grid.getChildren().removeIf(child ->
                child instanceof ImageView &&
                        GridPane.getRowIndex(child) != null &&
                        GridPane.getColumnIndex(child) != null &&
                        GridPane.getRowIndex(child) == row &&
                        GridPane.getColumnIndex(child) == col
        );

        if (clickHandler != null)
            imageView.setOnMouseClicked(event -> clickHandler.accept(node));

        if (connectorView != null) {
            connectorView.setOnMouseClicked(event -> {
                assert clickHandler != null;
                clickHandler.accept(node);
            });
            grid.add(connectorView, col, row);
        }
        grid.add(imageView, col, row);
    }

    public static void loadMainMenu(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/main_menu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setTitle("Light Bulb Game");
        } catch (IOException e) {
            System.err.println("Error loading main menu: " + e.getMessage());
        }
    }

    public static void loadArchive(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/archive_menu.fxml"));
        Parent root = loader.load();
        ArchiveController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle("Light Bulb Game - Archive Menu");
        controller.showGames();
    }

    public static void startGame(int size, Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/game_board.fxml"));
        Parent root = loader.load();

        GameBoardController controller = loader.getController();
        controller.setBoardSize(size);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - " + size + "x" + size);
    }
}

