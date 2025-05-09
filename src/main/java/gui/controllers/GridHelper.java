/**
 * Helper class responsible for managing the game grid and various UI components of the Light Bulb Game.
 * <p>
 * This class handles the loading and caching of image resources, rendering and updating game nodes on
 * the JavaFX {@link GridPane}, managing click handlers, animations for rotating connectors, and transitioning
 * between different UI scenes (main menu, archive, game board). It also includes utility methods for undo and redo actions.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
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
    /**
     * A cache to store the images used for game elements.
     */
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

    /**
     * Loads and caches all images used in the game.
     * This includes images for bulbs, connectors, power nodes, and hints.
     * <p>
     * The images are loaded from resources and stored in a static map for later use.
     * </p>
     */
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

    /**
     * Retrieves an image from the cache by its key.
     *
     * @param str The key corresponding to the desired image.
     * @return The image if found, or {@code null} if not.
     */
    public static Image getImage(String str) {
        return imageCache.get(str);
    }

    /**
     * Creates and adds cells to the game grid based on the size and configuration of the game board.
     * Each cell represents a {@link GameNode} and is clickable to interact with the game.
     *
     * @param game The current game instance.
     * @param grid The {@link GridPane} to which the cells are added.
     * @param cellSize The size of each cell in pixels.
     * @param boardSize The size of the game board (number of rows and columns).
     * @param clickHandler A {@link Consumer} that handles the click event on the cells.
     */
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

    /**
     * Fills a single cell in the grid with an image corresponding to the {@link GameNode} it represents.
     * The image is rotated based on the node's configuration and state.
     *
     * @param game The current game instance.
     * @param grid The {@link GridPane} to which the cell is added.
     * @param cellSize The size of each cell in pixels.
     * @param row The row of the cell.
     * @param col The column of the cell.
     * @param clickHandler A {@link Consumer} that handles the click event on the cell.
     * @param animate Whether to animate the rotation of the image.
     * @param isUndo Whether the action is an undo operation.
     */
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

    /**
     * Loads and displays the main menu scene for the Light Bulb Game.
     * Initializes the {@link MainMenuController} and sets up the primary stage for the main menu.
     *
     * @param primaryStage The main stage of the application.
     */
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

    /**
     * Loads and displays the archive menu scene, showing previously saved games.
     * Initializes the {@link ArchiveController} and sets up the primary stage for the archive menu.
     *
     * @param primaryStage The main stage of the application.
     * @throws IOException If an error occurs while loading the scene.
     */
    public static void loadArchive(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/archive_menu.fxml"));
        Parent root = loader.load();
        ArchiveController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.setTitle("Light Bulb Game - Archive Menu");
        controller.showGames();
    }

    /**
     * Starts a new game with the specified board size.
     * Initializes the {@link GameBoardController} and sets up the primary stage for the game board.
     *
     * @param size The size of the game board (e.g., 5x5, 6x6).
     * @param primaryStage The main stage of the application.
     * @throws IOException If an error occurs while loading the scene.
     */
    public static void startGame(int size, Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/game_board.fxml"));
        Parent root = loader.load();

        GameBoardController controller = loader.getController();
        controller.setBoardSize(size);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - " + size + "x" + size);
    }

    /**
     * Performs an undo operation on the game, restoring the previous state.
     * If the undo operation is successful, the grid is updated accordingly.
     *
     * @param game The current game instance.
     * @param boardSize The size of the game board.
     * @param gameGrid The {@link GridPane} representing the game board.
     * @param cellSize The size of each cell in pixels.
     * @param clickHandler A {@link Consumer} that handles cell click events.
     * @param archive A flag indicating if the undo operation is related to the archive.
     * @return {@code true} if the undo operation was successful, {@code false} otherwise.
     */
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

    /**
     * Performs a redo operation on the game, restoring the next state after an undo.
     * If the redo operation is successful, the grid is updated accordingly.
     *
     * @param game The current game instance.
     * @param boardSize The size of the game board.
     * @param gameGrid The {@link GridPane} representing the game board.
     * @param cellSize The size of each cell in pixels.
     * @param clickHandler A {@link Consumer} that handles cell click events.
     * @param archive A flag indicating if the redo operation is related to the archive.
     * @return {@code true} if the redo operation was successful, {@code false} otherwise.
     */
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


    /**
     * Opens a modal dialog on top of the primary stage. The dialog can display additional content or options.
     *
     * @param dialogStage The stage for the modal dialog.
     * @param root The root node of the dialog's scene.
     * @param primaryStage The main stage of the application, used to center the dialog.
     */
    public static void openDialog(Stage dialogStage, Parent root, Stage primaryStage) {
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.show();
        double centerX = primaryStage.getX() + (primaryStage.getWidth() - dialogStage.getWidth()) / 2;
        double centerY = primaryStage.getY() + (primaryStage.getHeight() - dialogStage.getHeight()) / 2;
        dialogStage.setX(centerX);
        dialogStage.setY(centerY);
    }


    /**
     * Configures the grid's size and cell dimensions based on the board size.
     * Adjusts the grid's column and row constraints to ensure the cells are evenly sized.
     *
     * @param boardSize The size of the game board (number of rows and columns).
     * @param gameGrid The {@link GridPane} to be configured.
     * @param fiend_size The total available size for the grid (in pixels).
     */
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

    /**
     * Clears the game grid, removing all game elements and constraints.
     *
     * @param gameGrid The {@link GridPane} representing the game board to be cleared.
     */
    public static void clearGameGrid(GridPane gameGrid) {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();
    }

    /**
     * Updates the game grid after a cell has been clicked.
     * Re-renders the entire grid and highlights the clicked node with smooth animation if necessary.
     *
     * @param node The {@link GameNode} that was clicked.
     * @param boardSize The size of the game board.
     * @param game The current game instance.
     * @param gameGrid The {@link GridPane} representing the game board.
     * @param cellSize The size of each cell in pixels.
     * @param clickHandler A {@link Consumer} that handles cell click events.
     */
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

