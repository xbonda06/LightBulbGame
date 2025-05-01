package gui.controllers;

import common.GameNode;
import common.Position;
import game.Game;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
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
    private int selectedRow = -1;
    private int selectedCol = -1;
    private final Map<String, Image> imageCache = new HashMap<>();

    // UI components
    @FXML private GridPane gameGrid;
    @FXML private Label timerLabel;
    @FXML private Label stepsLabel;
    @FXML private Button hintButton;
    //temporary
    @FXML private Button addBulbButton;
    @FXML private Button addPowerButton;
    private Game game;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        setupTimer();
        loadImages();
        setupButtonStates();
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
        //game.init();

        clearGameGrid();
        setupGridConstraints();
        createCells();
        resetSelection();
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
        int cellSize = FIELD_SIZE / boardSize;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                final int currentRow = row;
                final int currentCol = col;
                final Rectangle cell = new Rectangle(cellSize - 2, cellSize - 2);

                cell.setFill(Color.web("#1D1033"));
                cell.setStroke(Color.BLACK);
                cell.setOnMouseClicked(event -> handleCellClick(currentRow, currentCol, cell));

                // Get the Node
                GameNode node = game.node(new Position(row + 1, col + 1));

                gameGrid.add(cell, col, row);

                if (node.isBulb() || node.isPower()) {
                    String imagePath = "";
                    double rotationAngle = 0;
                    if(node.isPower()) {
                        int count = 0;
                        if (node.north()) count++;
                        if (node.south()) count++;
                        if (node.east()) count++;
                        if (node.west()) count++;

                        if (count == 4) {
                            imagePath = POWER_4_IMAGE;
                        } else if (count == 3) {
                            imagePath = POWER_3_IMAGE;
                            if (!node.north())
                                rotationAngle = 180;
                            else if (!node.south())
                                rotationAngle = 0;
                            else if (!node.east())
                                rotationAngle = 270;
                            else
                                rotationAngle = 90;
                        } else if (count == 1) {
                            imagePath = POWER_1_IMAGE;
                            if (node.north()) rotationAngle = 0;
                            else if (node.south()) rotationAngle = 180;
                            else if(node.east()) rotationAngle = 90;
                            else rotationAngle = 270;
                        } else if ((node.north() && node.south()) || (node.east() && node.west())) {
                            imagePath = POWER_2ud_IMAGE;
                            if (node.north()) rotationAngle = 0;
                            else rotationAngle = 90;
                        } else if ((node.north() || node.south()) && (node.east() || node.west())) {
                            imagePath = POWER_2_IMAGE;
                            if (node.north() && node.east()) rotationAngle = 0;
                            else if (node.north() && node.west()) rotationAngle = 270;
                            else if (node.south() && node.east()) rotationAngle = 90;
                            else rotationAngle = 180;
                        }
                    }
                    if(node.isBulb()) {
                        imagePath = node.light() ? BULB_ON_IMAGE : BULB_OFF_IMAGE;
                    }
                    ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
                    imageView.setFitWidth(cellSize);
                    imageView.setFitHeight(cellSize);
                    imageView.setRotate(rotationAngle);
                    GridPane.setHalignment(imageView, HPos.CENTER);
                    GridPane.setValignment(imageView, VPos.CENTER);
                    gameGrid.add(imageView, col, row);
                }
                else {
                    String imagePath = SHORT_OFF_IMAGE;
                    double rotationAngle = 0;
                    HPos hAlign = HPos.CENTER;
                    VPos vAlign = VPos.CENTER;
                    ImageView imageView;

                    if (node.isCross()) {
                        imagePath = node.light() ? CROSS_ON_IMAGE : CROSS_OFF_IMAGE;
                    } else if (node.isHalfCross()) {
                        imagePath = node.light() ? HALF_CROSS_ON_IMAGE : HALF_CROSS_OFF_IMAGE;
                        if (node.north() && node.south() && node.east()) {
                            rotationAngle = 0;
                        } else if (node.north() && node.south() && node.west()) {
                            rotationAngle = 180;
                        } else if (node.east() && node.west() && node.north()) {
                            rotationAngle = 270;
                        } else if (node.east() && node.west() && node.south()) {
                            rotationAngle = 90;
                        }
                    } else if (node.isCorner()) {
                        imagePath = node.light() ? CORNER_ON_IMAGE : CORNER_OFF_IMAGE;
                        if (node.north() && node.east()) {
                            rotationAngle = 0;
                        }
                        else if (node.east() && node.south()) {
                            rotationAngle = 90;
                        }
                        else if (node.south() && node.west()) {
                            rotationAngle = 180;
                        }
                        else if (node.west() && node.north()) {
                            rotationAngle = 270;
                        }
                    } else if (node.isLong()) {
                        imagePath = node.light() ? LONG_ON_IMAGE : LONG_OFF_IMAGE;
                        if (node.east() && node.west()) rotationAngle = 90;
                        else if (node.north() && node.south()) rotationAngle = 0;
                    }

                    imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
                    imageView.setFitWidth(cellSize);
                    imageView.setFitHeight(cellSize);
                    imageView.setRotate(rotationAngle);
                    GridPane.setHalignment(imageView, hAlign);
                    GridPane.setValignment(imageView, vAlign);
                    gameGrid.add(imageView, col, row);
                }
            }
        }
    }

    private void handleCellClick(int row, int col, Rectangle cell) {
        selectedRow = row;
        selectedCol = col;
        stepsTaken++;

        updateStepsDisplay();
        updateButtonStates();
        cell.setFill(cell.getFill() == Color.WHITE ? Color.LIGHTGRAY : Color.WHITE);
    }

    private void setupButtonStates() {
        addBulbButton.setDisable(true);
        addPowerButton.setDisable(true);
    }

    private void updateButtonStates() {
        boolean isCellSelected = selectedRow >= 0 && selectedCol >= 0;
        addBulbButton.setDisable(!isCellSelected);
        addPowerButton.setDisable(!isCellSelected);
    }

    @FXML
    private void addBulb() {
        if (isValidSelection()) {
            addImageToCell("bulb_off", selectedRow, selectedCol);
        }
    }

    @FXML
    private void addPowerSource() {
        if (isValidSelection()) {
            addImageToCell("power_on", selectedRow, selectedCol);
        }
    }

    private boolean isValidSelection() {
        return selectedRow >= 0 && selectedCol >= 0 &&
                selectedRow < boardSize && selectedCol < boardSize;
    }

    private void addImageToCell(String imageKey, int row, int col) {
        removeExistingImages(row, col);

        Image image = imageCache.get(imageKey);
        if (image != null) {
            ImageView imageView = createImageView(image);
            GridPane.setHalignment(imageView, HPos.CENTER);
            GridPane.setValignment(imageView, VPos.CENTER);
            gameGrid.add(imageView, col, row);
        }
    }

    private void removeExistingImages(int row, int col) {
        gameGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) == row &&
                        GridPane.getColumnIndex(node) == col &&
                        node instanceof ImageView);
    }

    private ImageView createImageView(Image image) {
        int cellSize = FIELD_SIZE / boardSize;
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(cellSize - 20);
        imageView.setFitHeight(cellSize - 20);
        imageView.setPreserveRatio(true);
        return imageView;
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

    private void resetSelection() {
        selectedRow = -1;
        selectedCol = -1;
        updateButtonStates();
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