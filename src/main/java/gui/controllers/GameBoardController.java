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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controller that manages the main game screen of the Light Bulb puzzle game.
 * Handles game initialization, player interactions, timer updates, win condition logic,
 * undo/redo functionality, hint window management, and transitions to/from the main menu.
 * <p>
 * Core features:
 * - Initializes a new game or continues from an archived state.
 * - Listens to user clicks and rotates connectors accordingly.
 * - Animates cell rotation and tracks game steps and time.
 * - Displays a hint window showing solution suggestions.
 * - Shows a win dialog when the puzzle is completed.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class GameBoardController {
    // Game constants
    private static final int FIELD_SIZE = 400;

    // Game state
    private int boardSize = 5;
    private int cellSize;
    private int secondsElapsed = 0;
    private int stepsTaken = 0;
    Stage primaryStage;
    private Timeline gameTimer;
    private Game game;
    private boolean hints_on = false;
    private boolean disableGame = false;
    private boolean fromArchive = false;

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

    /**
     * Sets the reference to the primary application stage.
     *
     * @param primaryStage the main stage of the application
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Sets whether the game is loaded from an archived state.
     *
     * @param fromArchive true if the game is loaded from the archive, false otherwise
     */
    public void setFromArchive(boolean fromArchive) { this.fromArchive = fromArchive;}

    /**
     * Initializes the game controller and sets up the timer.
     */
    @FXML
    public void initialize() {
        setupTimer();
        GridHelper.loadImages();
    }

    /**
     * Sets the board size for the game.
     *
     * @param size the size of the board (e.g., 5 for a 5x5 grid)
     */
    public void setBoardSize(int size) {
        this.boardSize = size;
        resetGame();
    }


    /**
     * Sets the current game object.
     *
     * @param game the game object to be used in this controller
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Initializes a timer that updates the display every second to track the elapsed game time.
     */
    private void setupTimer() {
        gameTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    secondsElapsed++;
                    updateTimerDisplay();
                })
        );
        gameTimer.setCycleCount(Animation.INDEFINITE);
    }


    /**
     * Starts a new game by resetting the board and starting the timer.
     */
    private void resetGame() {
        stopTimer();
        createGameBoard();
        startTimer();
        this.disableGame = false;
    }


    /**
     * Initializes the game board, either by creating a new game or by using an archived game.
     */
    private void createGameBoard() {
        if(!fromArchive) {
            this.game = Game.generate(boardSize, boardSize);
            this.game.randomizeRotations();
        }
        this.cellSize = FIELD_SIZE / boardSize;
        fromArchive = false;
        GridHelper.clearGameGrid(gameGrid);
        GridHelper.setupGridConstraints(boardSize, gameGrid, FIELD_SIZE);
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, this::handleCellClick);
    }

    /**
     * Handles the rotation of the clicked node by 90 degrees and updates the board.
     *
     * @param node the node clicked by the user
     */
    private void handleCellClick(GameNode node) {
        if(!disableGame){
            stepsTaken++;
            updateStepsDisplay();
            game.setLastTurnedNode(node.getPosition());
            node.turn();
            GridHelper.updateAfterClick(node, boardSize, game, gameGrid, cellSize, this::handleCellClick);
            int row = node.getPosition().getRow() - 1;
            int col = node.getPosition().getCol() - 1;
            if (this.hints_on)
                hintsController.reloadHints(game, row, col);
            if (game.checkWin()){
                gameWin();
            }
        }
    }


    /**
     * Handles the win condition, stops the timer, and displays the victory dialog.
     */
    private void gameWin() {
        stopTimer();
        closeHintsAndCenterMain();
        this.disableGame = true;
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> showVictoryDialog());
        pause.play();
    }

    /**
     * Displays the victory dialog window when the player wins.
     * Allows the user to either reset the game or return to the main menu.
     */
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
            GridHelper.openDialog(dialogStage, root, primaryStage);
            dialogStage.setOnHidden(e -> rootPane.getChildren().remove(overlay));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens the hint window for the game, showing the solution suggestions.
     */
    @FXML
    private void useHint() {
        if (hintsStage == null || !hintsStage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/hints.fxml"));
                Parent root = loader.load();

                hintsController = loader.getController();
                hintsController.init(game, cellSize, boardSize);

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

    /**
     * Resets the game board, closes any open hint windows, and restarts the game.
     */
    @FXML
    private void resetBoard() {
        closeHintsAndCenterMain();
        resetGame();
    }

    /**
     * Transitions to the main menu.
     * <p>
     * Stops the timer, closes the hint window, and loads the main menu scene.
     * </p>
     */
    @FXML
    private void toMainMenu() {
        stopTimer();
        closeHintsAndCenterMain();
        loadMainMenu();
    }


    /**
     * Loads the main menu scene.
     */
    private void loadMainMenu() {
        GridHelper.loadMainMenu(primaryStage);
    }

    /**
     * Starts the game timer and initializes step count and display.
     */
    private void startTimer() {
        secondsElapsed = 0;
        stepsTaken = 0;
        updateTimerDisplay();
        updateStepsDisplay();
        updateHintsDisplay();
        gameTimer.play();
    }

    /**
     * Stops the game timer.
     */
    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    /**
     * Updates the timer display with the current elapsed time.
     */
    private void updateTimerDisplay() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
    }

    /**
     * Updates the steps display with the current number of steps taken.
     */
    private void updateStepsDisplay() {
        stepsLabel.setText(String.format("Steps: %d/%d", stepsTaken, game.turnsToWin()));
    }


    /**
     * Updates the hint button display.
     */
    private void updateHintsDisplay() {
        hintButton.setText("HINTS");
    }

    /**
     * Closes the hint window and re-centers the main game window.
     */
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

    /**
     * Creates a timeline for animating the main stage's movement to its new position.
     *
     * @param mainStage the main stage
     * @param centerX the new x-coordinate
     * @param centerY the new y-coordinate
     * @return the timeline animation
     */
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

    /**
     * Handles the undo action by reverting the game state to the previous step.
     * <p>
     * Calls the undo method in the GridHelper class to undo the last move.
     * The game board is updated accordingly, and the state of the game is reverted.
     * </p>
     */
    @FXML public void getUndo() {
        GridHelper.undo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }

    /**
     * Handles the redo action by reapplying the last undone move.
     * <p>
     * Calls the redo method in the GridHelper class to redo the previous undo.
     * The game board is updated accordingly, and the state of the game is restored.
     * </p>
     */
    @FXML public void getRedo() {
        GridHelper.redo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }
}