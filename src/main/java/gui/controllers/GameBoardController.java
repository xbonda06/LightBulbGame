/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * This controller manages the main game screen of the Light Bulb puzzle game.
 * It handles game initialization, player interaction with the board, timer updates,
 * and win condition logic. The controller also supports undo/redo functionality,
 * hint window management, and transitions to/from the main menu.
 *
 * Core features:
 * - Initializes a new game or continues from an archived state.
 * - Listens to user clicks and rotates connectors accordingly.
 * - Animates cell rotation and tracks game steps and time.
 * - Displays a hint window showing solution suggestions.
 * - Shows a win dialog when the puzzle is completed.
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setFromArchive(boolean fromArchive) { this.fromArchive = fromArchive;}

    @FXML
    public void initialize() {
        setupTimer();
        GridHelper.loadImages();
    }

    // Sets size based on the selected game (passed from MainMenuController)
    public void setBoardSize(int size) {
        this.boardSize = size;
        resetGame();
    }

    public void setGame(Game game) {
        this.game = game;
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

    // Start new game
    private void resetGame() {
        stopTimer();
        createGameBoard();
        startTimer();
        this.disableGame = false;
    }

    // Initialize a new game with a ready board and randomly rotated connectors
    private void createGameBoard() {
        if(!fromArchive) {
            this.game = Game.generate(boardSize, boardSize);
            this.game.randomizeRotations();
        }
        this.cellSize = FIELD_SIZE / boardSize;
        fromArchive = false;
        clearGameGrid();
        setupGridConstraints();
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, this::handleCellClick);
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
                    GridHelper.fillCell(game, gameGrid, cellSize, r, c, this::handleCellClick,
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
            GridHelper.openDialog(dialogStage, root, primaryStage);
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
        hintButton.setText("HINTS");
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
        GridHelper.undo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }

    @FXML public void getRedo() {
        GridHelper.redo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }
}