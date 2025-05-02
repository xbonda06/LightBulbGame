package gui.controllers;

import common.GameNode;
import common.Position;
import game.Game;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameBoardController {
    // Game constants
    private static final int FIELD_SIZE = 400;
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


    // Game state
    private int boardSize = 5;
    private Stage primaryStage;
    private Timeline gameTimer;
    private int secondsElapsed = 0;
    private int hintsUsed = 0;
    private int stepsTaken = 0;
    private final Map<String, Image> imageCache = new HashMap<>();

    // UI components
    @FXML private GridPane gameGrid;
    @FXML private Label timerLabel;
    @FXML private Label stepsLabel;
    @FXML private Button hintButton;

    private Game game;
    private int cellSize;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        setupTimer();
        loadImages();
    }

    public void setBoardSize(int size) {
        this.boardSize = size;
        resetGame();
    }

    private void setupTimer() {
        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    secondsElapsed++;
                    updateTimerDisplay();
                })
        );
        gameTimer.setCycleCount(Animation.INDEFINITE);
    }

    private void loadImages() {
        try {
            imageCache.put("bulb_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(BULB_OFF_IMAGE))));
            imageCache.put("bulb_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(BULB_ON_IMAGE))));

            imageCache.put("power_1", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_1_IMAGE))));
            imageCache.put("power_2", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_2_IMAGE))));
            imageCache.put("power_2ud", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_2ud_IMAGE))));
            imageCache.put("power_3", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_3_IMAGE))));
            imageCache.put("power_4", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_4_IMAGE))));

            imageCache.put("cross_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(CROSS_OFF_IMAGE))));
            imageCache.put("half_cross_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(HALF_CROSS_OFF_IMAGE))));
            imageCache.put("corner_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(CORNER_OFF_IMAGE))));
            imageCache.put("long_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(LONG_OFF_IMAGE))));
            imageCache.put("short_off", new Image(Objects.requireNonNull(getClass().getResourceAsStream(SHORT_OFF_IMAGE))));

            imageCache.put("cross_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(CROSS_ON_IMAGE))));
            imageCache.put("half_cross_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(HALF_CROSS_ON_IMAGE))));
            imageCache.put("corner_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(CORNER_ON_IMAGE))));
            imageCache.put("long_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(LONG_ON_IMAGE))));
            imageCache.put("short_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(SHORT_ON_IMAGE))));
        } catch (NullPointerException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void resetGame() {
        stopTimer();
        createGameBoard();
        startTimer();
    }

    private void createGameBoard() {
        this.game = Game.generate(boardSize, boardSize);
        this.game.randomizeRotations();

        clearGameGrid();
        setupGridConstraints();
        createCells();
    }

    private void clearGameGrid() {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();
    }

    private void setupGridConstraints() {
        int cellSize = FIELD_SIZE / boardSize;
        gameGrid.setMinSize(FIELD_SIZE, FIELD_SIZE);
        gameGrid.setPrefSize(FIELD_SIZE, FIELD_SIZE);
        gameGrid.setMaxSize(FIELD_SIZE, FIELD_SIZE);

        for (int i = 0; i < boardSize; i++) {
            ColumnConstraints colConst = new ColumnConstraints(cellSize, cellSize, cellSize);
            RowConstraints rowConst = new RowConstraints(cellSize, cellSize, cellSize);
            gameGrid.getColumnConstraints().add(colConst);
            gameGrid.getRowConstraints().add(rowConst);
        }
    }

    private void createCells() {
        this.cellSize = FIELD_SIZE / boardSize;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                GameNode node = game.node(new Position(row + 1, col + 1));

                Rectangle cell = new Rectangle(cellSize - 2, cellSize - 2);
                cell.setFill(Color.web("#1D1033"));
                cell.setStroke(Color.BLACK);
                cell.setOnMouseClicked(event -> handleCellClick(node));

                gameGrid.add(cell, col, row);
                fillCell(row, col);
            }
        }
    }

    private void fillCell(int row, int col) {
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
        imageView.setRotate(rotationAngle);
        imageView.setPreserveRatio(false);

        gameGrid.getChildren().removeIf(child ->
                child instanceof ImageView &&
                        GridPane.getRowIndex(child) != null &&
                        GridPane.getColumnIndex(child) != null &&
                        GridPane.getRowIndex(child) == row &&
                        GridPane.getColumnIndex(child) == col
        );
        if (connectorView != null)
            gameGrid.add(connectorView, col, row);
        gameGrid.add(imageView, col, row);
    }

    private void handleCellClick(GameNode node) {
        stepsTaken++;
        updateStepsDisplay();
        node.turn();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                fillCell(r, c);
            }
        }
    }

    @FXML
    private void useHint() {
        if (hintsUsed < 2) {
            hintsUsed++;
            updateHintsDisplay();
        }
    }

    @FXML
    private void resetBoard() {
        resetGame();
    }

    @FXML
    private void toMainMenu() {
        stopTimer();
        loadMainMenu();
    }

    private void loadMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setTitle("Light Bulb Game");
        } catch (IOException e) {
            System.err.println("Error loading main menu: " + e.getMessage());
        }
    }


    private void startTimer() {
        secondsElapsed = 0;
        stepsTaken = 0;
        hintsUsed = 0;
        updateTimerDisplay();
        updateStepsDisplay();
        updateHintsDisplay();
        gameTimer.play();
    }

    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void updateTimerDisplay() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void updateStepsDisplay() {
        stepsLabel.setText(String.format("Steps: %d/25", stepsTaken));
    }

    private void updateHintsDisplay() {
        hintButton.setText(String.format("HINT %d/2", hintsUsed));
    }
}