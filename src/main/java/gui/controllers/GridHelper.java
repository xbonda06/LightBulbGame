/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * Helper Abstract class for managing the game grid in the Light Bulb Game.
 *
 * It handles loading and caching of image resources, rendering and updating
 * game nodes on the JavaFX GridPane, managing click handlers, animations for
 * rotating connectors, and transitioning between different UI scenes (main menu,
 * archive, game board).
 */

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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class GridHelper {
    public static final Map<String, Image> imageCache = new HashMap<>();

    private static final String BULB_OFF_IMAGE = "/images/bulb_off.png";
    private static final String BULB_ON_IMAGE = "/images/bulb_on.png";
    private static final String POWER_1_IMAGE = "/images/power/power_1.png";
    private static final String POWER_2_IMAGE = "/images/power/power_2.png";
    private static final String POWER_2ud_IMAGE = "/images/power/power_2ud.png";
    private static final String POWER_3_IMAGE = "/images/power/power_3.png";
    private static final String POWER_4_IMAGE = "/images/power/power_4.png";

    private static final String CROSS_OFF_IMAGE = "/images/connectors_off/cross.png";
    private static final String HALF_CROSS_OFF_IMAGE = "/images/connectors_off/half_cross.png";
    private static final String CORNER_OFF_IMAGE = "/images/connectors_off/corner.png";
    private static final String LONG_OFF_IMAGE = "/images/connectors_off/long.png";
    private static final String SHORT_OFF_IMAGE = "/images/connectors_off/short.png";

    private static final String CROSS_ON_IMAGE = "/images/connectors_on/cross.png";
    private static final String HALF_CROSS_ON_IMAGE = "/images/connectors_on/half_cross.png";
    private static final String CORNER_ON_IMAGE = "/images/connectors_on/corner.png";
    private static final String LONG_ON_IMAGE = "/images/connectors_on/long.png";
    private static final String SHORT_ON_IMAGE = "/images/connectors_on/short.png";

    private static final String HINT1_IMAGE = "/images/hint/hint1.png";
    private static final String HINT2_IMAGE = "/images/hint/hint2.png";
    private static final String HINT3_IMAGE = "/images/hint/hint3.png";

    public static void loadImages() {
        try {
            imageCache.put("bulb_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(BULB_OFF_IMAGE))));
            imageCache.put("bulb_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(BULB_ON_IMAGE))));

            imageCache.put("power_1", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(POWER_1_IMAGE))));
            imageCache.put("power_2", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(POWER_2_IMAGE))));
            imageCache.put("power_2ud", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(POWER_2ud_IMAGE))));
            imageCache.put("power_3", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(POWER_3_IMAGE))));
            imageCache.put("power_4", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(POWER_4_IMAGE))));

            imageCache.put("cross_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(CROSS_OFF_IMAGE))));
            imageCache.put("half_cross_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(HALF_CROSS_OFF_IMAGE))));
            imageCache.put("corner_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(CORNER_OFF_IMAGE))));
            imageCache.put("long_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(LONG_OFF_IMAGE))));
            imageCache.put("short_off", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(SHORT_OFF_IMAGE))));

            imageCache.put("cross_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(CROSS_ON_IMAGE))));
            imageCache.put("half_cross_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(HALF_CROSS_ON_IMAGE))));
            imageCache.put("corner_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(CORNER_ON_IMAGE))));
            imageCache.put("long_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(LONG_ON_IMAGE))));
            imageCache.put("short_on", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(SHORT_ON_IMAGE))));

            imageCache.put("hint1", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(HINT1_IMAGE))));
            imageCache.put("hint2", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(HINT2_IMAGE))));
            imageCache.put("hint3", new Image(Objects.requireNonNull(GridHelper.class.getResourceAsStream(HINT3_IMAGE))));
        } catch (NullPointerException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    public static Image getImage(String str) {
        return imageCache.get(str);
    }

    public static void createCells(Game game, GridPane grid, int cellSize, int boardSize, Consumer<GameNode> clickHandler) {

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                GameNode node = game.node(new Position(row + 1, col + 1));
                Rectangle cell = new Rectangle(cellSize - 2, cellSize - 2);
                cell.setFill(Color.web("#1D1033"));
                cell.setStroke(Color.BLACK);
                if (clickHandler != null)
                    cell.setOnMouseClicked(event -> clickHandler.accept(node));

                grid.add(cell, col, row);
                fillCell(game, grid, cellSize, row, col, clickHandler, false, false);
            }
        }
    }

    public static void fillCell(Game game, GridPane grid, int cellSize, int row, int col, Consumer<GameNode> clickHandler, boolean animate, boolean isUndo) {
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

            double previousRotation = isUndo ? (rotationAngle + 90 + 360) % 360 : (rotationAngle - 90 + 360) % 360;
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
            if (clickHandler != null){
                connectorView.setOnMouseClicked(event -> clickHandler.accept(node));
            }
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

    public static boolean undo(Game game, int boardSize, GridPane gameGrid, int cellSize,Consumer<GameNode> clickHandler, boolean archive) {
        boolean undo = archive ? game.undoArchive() : game.undo();
        if (undo){
            int row = game.getLastTurnedNode().getRow() - 1;
            int col = game.getLastTurnedNode().getCol() - 1;
            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    boolean animation = r == row && c == col;
                    GridHelper.fillCell(game, gameGrid, cellSize, r, c, clickHandler,
                            animation, !archive);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean redo(Game game, int boardSize, GridPane gameGrid, int cellSize,Consumer<GameNode> clickHandler, boolean archive) {
        boolean redo = archive ? game.redoArchive() : game.redo();
        if(redo) {
            int row = game.getLastTurnedNode().getRow() - 1;
            int col = game.getLastTurnedNode().getCol() - 1;
            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    boolean animation = r == row && c == col;
                    GridHelper.fillCell(game, gameGrid, cellSize, r, c, clickHandler,
                            animation, archive);
                }
            }
            return true;
        }
        return false;
    }

    public static void openDialog(Stage dialogStage, Parent root, Stage primaryStage) {
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.show();
        double centerX = primaryStage.getX() + (primaryStage.getWidth() - dialogStage.getWidth()) / 2;
        double centerY = primaryStage.getY() + (primaryStage.getHeight() - dialogStage.getHeight()) / 2;
        dialogStage.setX(centerX);
        dialogStage.setY(centerY);
    }

    // Configure grid size and set fixed cell dimensions based on the board size
    public static void setupGridConstraints(int boardSize, GridPane gameGrid, int fiend_size) {
        int cellSize = fiend_size / boardSize;
        gameGrid.setMinSize(fiend_size, fiend_size);
        gameGrid.setPrefSize(fiend_size, fiend_size);
        gameGrid.setMaxSize(fiend_size, fiend_size);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints colConst = new ColumnConstraints(cellSize, cellSize, cellSize);
            RowConstraints rowConst = new RowConstraints(cellSize, cellSize, cellSize);
            gameGrid.getColumnConstraints().add(colConst);
            gameGrid.getRowConstraints().add(rowConst);
        }
    }

    // Delete game
    public static void clearGameGrid(GridPane gameGrid) {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();
    }

    public static void updateAfterClick(GameNode node, int boardSize, Game game, GridPane gameGrid, int cellSize,Consumer<GameNode> clickHandler) {
        int row = node.getPosition().getRow() - 1;
        int col = node.getPosition().getCol() - 1;
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                boolean animate = (r == row && c == col); //for smooth rotation
                GridHelper.fillCell(game, gameGrid, cellSize, r, c, clickHandler,
                        animate, false);
            }
        }
    }
}

