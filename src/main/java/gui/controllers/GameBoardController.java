package gui.controllers;

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
    private static final String POWER_ON_IMAGE = "/images/power_on.png";

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
    private Map<String, Image> imageCache = new HashMap<>();

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
            imageCache.put("power_on", new Image(Objects.requireNonNull(getClass().getResourceAsStream(POWER_ON_IMAGE))));
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
        this.game = Game.create(boardSize, boardSize);
        game.init();

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

                cell.setFill(Color.WHITE);
                cell.setStroke(Color.BLACK);

                cell.setOnMouseClicked(event -> handleCellClick(currentRow, currentCol, cell));

                gameGrid.add(cell, col, row);
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