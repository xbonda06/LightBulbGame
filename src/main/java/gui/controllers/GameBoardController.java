/*
* Author: Olha Tomylko (xtomylo00)
*
* Description:
*/

package gui.controllers;

import common.GameNode;
import game.Game;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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

    private static final String HINT1_IMAGE = "/images/hint/hint1.png";
    private static final String HINT2_IMAGE = "/images/hint/hint2.png";
    private static final String HINT3_IMAGE = "/images/hint/hint3.png";

    // Game state
    private int boardSize = 5;
    private int cellSize;
    private int secondsElapsed = 0;
    private int hintsUsed = 0;
    private int stepsTaken = 0;
    private final Map<String, Image> imageCache = new HashMap<>();
    private Stage primaryStage;
    private Timeline gameTimer;
    private Game game;
    private boolean hints_on = false;
    private boolean disableGame = false;

    private Stage hintsStage = null;
    private HintsController hintsController = null;

    // UI components
    @FXML private GridPane gameGrid;
    @FXML private Label timerLabel;
    @FXML private Label stepsLabel;
    @FXML private Button hintButton;
    @FXML public StackPane rootPane;
    @FXML public Button undoButton;
    @FXML public Button redoButton;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        setupTimer();
        loadImages();
    }

    // Sets size based on the selected game (passed from MainMenuController)
    public void setBoardSize(int size) {
        this.boardSize = size;
        resetGame();
    }

    // Initializes a timer that updates the display every second to track elapsed game time.
    private void setupTimer() {
        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    secondsElapsed++;
                    updateTimerDisplay();
                })
        );
        gameTimer.setCycleCount(Animation.INDEFINITE);
    }

    // Load images to cache
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

            imageCache.put("hint1", new Image(Objects.requireNonNull(getClass().getResourceAsStream(HINT1_IMAGE))));
            imageCache.put("hint2", new Image(Objects.requireNonNull(getClass().getResourceAsStream(HINT2_IMAGE))));
            imageCache.put("hint3", new Image(Objects.requireNonNull(getClass().getResourceAsStream(HINT3_IMAGE))));
        } catch (NullPointerException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    // Start new game
    private void resetGame() {
        stopTimer();
        createGameBoard();
        startTimer();
        this.disableGame = false;
    }

    // Initialize a new game with a ready board and randomly rotated connectors
    private void createGameBoard() {
        this.game = Game.generate(boardSize, boardSize);
        this.game.randomizeRotations();
        this.cellSize = FIELD_SIZE / boardSize;

        clearGameGrid();
        setupGridConstraints();
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, this::handleCellClick, imageCache);
    }

    // Delete game
    private void clearGameGrid() {
        gameGrid.getChildren().clear();
        gameGrid.getColumnConstraints().clear();
        gameGrid.getRowConstraints().clear();
    }

    // Configure grid size and set fixed cell dimensions based on the board size
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

    // Rotate the clicked node by 90 degrees and refresh the game board to reflect the change
    private void handleCellClick(GameNode node) {
        if(!disableGame){
            stepsTaken++;
            updateStepsDisplay();
            game.setLastTurnedNode(node.getPosition());
            node.turn();
            int row = node.getPosition().getRow() - 1;
            int col = node.getPosition().getCol() - 1;
            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    boolean animate = (r == row && c == col); //for smooth rotation
                    GridHelper.fillCell(game, gameGrid, cellSize, imageCache, r, c, this::handleCellClick,
                            animate, false);
                }
            }
            if (this.hints_on)
                hintsController.reloadHints(game, row, col);
            if (game.checkWin()){
                gameWin();
            }
        }
    }

    // Waits for 0.7 seconds and then displays a modal dialog with a "YOU WON!" message
    private void gameWin() {
        stopTimer();
        closeHintsAndCenterMain();
        this.disableGame = true;
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> showVictoryDialog());
        pause.play();
    }

    // Displays a modal victory dialog window when the player wins.
    // Sets up the controller with appropriate actions
    // (resetting the game or returning to the main menu), and shows the dialog as a blocking window.
    private void showVictoryDialog() {
        try {
            Rectangle overlay = new Rectangle(rootPane.getWidth(), rootPane.getHeight(), Color.rgb(0, 0, 0, 0.7));
            overlay.widthProperty().bind(rootPane.widthProperty());
            overlay.heightProperty().bind(rootPane.heightProperty());
            rootPane.getChildren().add(overlay);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/victory_dialog.fxml"));
            Parent root = loader.load();

            VictoryDialogController controller = loader.getController();

            Stage dialogStage = new Stage(StageStyle.UNDECORATED);
            controller.setDialogStage(dialogStage);

            controller.setOnYesAction(this::resetGame);
            controller.setOnNoAction(this::loadMainMenu);

            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.show();
            double centerX = primaryStage.getX() + (primaryStage.getWidth() - dialogStage.getWidth()) / 2;
            double centerY = primaryStage.getY() + (primaryStage.getHeight() - dialogStage.getHeight()) / 2;
            dialogStage.setX(centerX);
            dialogStage.setY(centerY);

            dialogStage.setOnHidden(e -> rootPane.getChildren().remove(overlay));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void useHint() {
        if (hintsStage == null || !hintsStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hints.fxml"));
                Parent root = loader.load();

                hintsController = loader.getController();
                hintsController.init(game, cellSize, boardSize, imageCache);

                hintsStage = new Stage();
                hintsStage.setTitle("Hints");
                hintsStage.setScene(new Scene(root, 600, 600));
                hintsStage.setResizable(false);

                Stage mainStage = (Stage) gameGrid.getScene().getWindow();
                double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
                double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

                AtomicReference<Double> totalWidth = new AtomicReference<>((double) 800 + 600 + 10);
                AtomicReference<Double> startX = new AtomicReference<>((screenWidth - totalWidth.get()) / 2.0);
                AtomicReference<Double> centerY = new AtomicReference<>((screenHeight - mainStage.getHeight()) / 2.0);

                Timeline moveMain = getTimeline(mainStage, startX.get(), centerY.get());
                moveMain.play();

                hintsStage.setX(startX.get() + 800 + 10);
                hintsStage.setY(centerY.get());
                hintsStage.show();
                hintsStage.setOnCloseRequest(e -> closeHintsAndCenterMain());
                this.hints_on = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void resetBoard() {
        closeHintsAndCenterMain();
        resetGame();
    }

    @FXML
    private void toMainMenu() {
        stopTimer();
        closeHintsAndCenterMain();
        loadMainMenu();
    }

    // Switch to the Main Menu scene
    private void loadMainMenu() {
        GridHelper.loadMainMenu(primaryStage);
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

    private void closeHintsAndCenterMain() {
        if (hintsStage != null) {
            hintsStage.close();
            hintsStage = null;
            hintsController = null;
            hints_on = false;
        }

        Stage mainStage = (Stage) gameGrid.getScene().getWindow();
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        double mainWidth = mainStage.getWidth();
        double mainHeight = mainStage.getHeight();

        double centerX = (screenWidth - mainWidth) / 2.0;
        double centerY = (screenHeight - mainHeight) / 2.0;

        //Smooth aligning 
        Timeline moveMain = getTimeline(mainStage, centerX, centerY);
        moveMain.play();
    }

    private static Timeline getTimeline(Stage mainStage, double centerX, double centerY) {
        DoubleProperty stageX = new SimpleDoubleProperty(mainStage.getX());
        DoubleProperty stageY = new SimpleDoubleProperty(mainStage.getY());

        stageX.addListener((obs, oldVal, newVal) -> mainStage.setX(newVal.doubleValue()));
        stageY.addListener((obs, oldVal, newVal) -> mainStage.setY(newVal.doubleValue()));

        return new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(stageX, centerX),
                        new KeyValue(stageY, centerY)
                )
        );
    }

    @FXML public void getUndo() {
        boolean undo = game.undo();
        if (undo){
            int row = game.getLastTurnedNode().getRow() - 1;
            int col = game.getLastTurnedNode().getCol() - 1;
            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    boolean animation = r == row && c == col;
                    GridHelper.fillCell(game, gameGrid, cellSize, imageCache, r, c, this::handleCellClick,
                            animation, true);
                }
            }
        }
    }

    @FXML public void getRedo() {
        boolean redo = game.redo();
        if(redo) {
            int row = game.getLastTurnedNode().getRow() - 1;
            int col = game.getLastTurnedNode().getCol() - 1;
            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    boolean animation = r == row && c == col;
                    GridHelper.fillCell(game, gameGrid, cellSize, imageCache, r, c, this::handleCellClick,
                            animation, false);
                }
            }
        }
    }
}