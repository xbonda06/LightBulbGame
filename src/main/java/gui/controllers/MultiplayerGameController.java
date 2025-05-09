package gui.controllers;

import common.GameNode;
import game.Game;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import multiplayer.GameClient;
import multiplayer.GameServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the multiplayer game screen.
 * <p>
 * Handles the game board UI, tracks player moves, displays opponents' game windows,
 * manages communication with the client/server, and detects game win events.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MultiplayerGameController implements GameWinListener {
    private static final int FIELD_SIZE = 400;
    private int secondsElapsed = 0;
    private int stepsTaken = 0;
    private Timeline gameTimer;
    private int cellSize;
    private Stage primaryStage;
    private GameServer server;
    private GameClient client;
    private Game game;
    private final int boardSize = 5;
    private final List<Stage> opponentStages = new ArrayList<>();


    /**
     * Sets the primary JavaFX stage.
     * @param primaryStage main stage for the game screen
     */
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    /**
     * Sets the game server instance.
     * @param server reference to the server
     */
    public void setServer(GameServer server) {this.server = server;}

    /**
     * Sets the game client instance.
     * @param client reference to the client
     */
    public void setClient(GameClient client) {this.client = client;}

    @FXML public StackPane rootPane;
    @FXML public GridPane gameGrid;
    @FXML public Button undoButton;
    @FXML public Button redoButton;
    @FXML public Label stepsLabel;
    @FXML public Label timerLabel;

    /**
     * Initializes the game screen after joining a multiplayer session.
     * Sets up the timer, loads the player's and opponents' game boards.
     * @throws IOException if FXML loading fails
     */
    public void showGame() throws IOException {
        this.client.setGameWinListener(this);
        this.game = client.getOwnGame();
        setupTimer();
        startTimer();

        opponentsGame();
        createGameBoard();
    }

    /**
     * Callback for when a player wins the game.
     * Displays a victory dialog with the winner's information.
     * @param winnerId the ID of the player who won
     */
    @Override
    public void onGameWin(int winnerId) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/win_multiplayer.fxml"));
                Parent root = loader.load();

                MultiplayerWinController controller = loader.getController();
                controller.setClient(client);
                controller.setStages(primaryStage, opponentStages);
                controller.setServer(server);
                controller.setWinnerId(winnerId);

                Stage dialogStage = new Stage(StageStyle.UNDECORATED);
                controller.setDialogStage(dialogStage);
                dialogStage.setTitle("Victory");
                GridHelper.openDialog(dialogStage, root, primaryStage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * Displays opponents' game windows based on their player IDs and positions them on the screen.
     * @throws IOException if loading opponent FXML fails
     */
    private void opponentsGame() throws IOException {
        client.requestPlayerCount();
        int count = client.getLatestPlayerCount();
        List<Integer> ids = client.getLatestPlayerIds();
        ids.remove(Integer.valueOf(client.getPlayerId()));

        double mainWidth = 800;
        double mainHeight = 600;
        double opponentWidth = 350;
        double opponentHeight = 350;
        double spacing = 10;

        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

        double mainX = (screenWidth - mainWidth) / 2.0;
        double mainY = (screenHeight - mainHeight) / 2.0;
        primaryStage.setX(mainX);
        primaryStage.setY(mainY);

        if (count > 1) {
            showOpponentWindow(ids.getFirst(),
                    mainX - opponentWidth - spacing,
                    mainY - 50);
        }
        if (count > 2) {
            showOpponentWindow(ids.get(1),
                    mainX - opponentWidth - spacing,
                    mainY + (opponentHeight) + spacing);
        }
        if (count > 3) {
            showOpponentWindow(ids.get(2),
                    mainX + mainWidth + spacing,
                    mainY - 50);
        }
    }

    /**
     * Creates and displays a window with a single opponent's game board.
     * @param id opponent's player ID
     * @param x  X position on the screen
     * @param y  Y position on the screen
     * @throws IOException if FXML loading fails
     */
    private void showOpponentWindow(Integer id, double x, double y) throws IOException {
        Game gameOpponent = client.getOpponentGame(id);
        if (gameOpponent == null) {
            System.out.println("Opponent game is null");
        }
        if (gameOpponent != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/multiplayer_other.fxml"));
            Parent root = loader.load();

            MultiplayerOpponentGameController opponentController = loader.getController();
            opponentController.setGame(client.getOpponentGame(id));
            opponentController.setGameClient(client);
            opponentController.showGame();
            opponentController.playerId.setText("Player " + id);

            Stage opponentStage = new Stage();
            opponentStage.setTitle("Player " + id);
            opponentStage.setScene(new Scene(root, 350, 350));
            opponentStage.setResizable(false);
            opponentStage.setX(x);
            opponentStage.setY(y);
            opponentStage.show();

            opponentStages.add(opponentStage);
        }
    }

    /**
     * Triggers an undo action for the player and updates the UI.
     */
    @FXML public void getUndo() {
        client.sendUndo();
        GridHelper.undo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }

    /**
     * Triggers a redo action for the player and updates the UI.
     */
    @FXML public void getRedo() {
        client.sendRedo();
        GridHelper.redo(game, boardSize, gameGrid, cellSize, this::handleCellClick, false);
    }


    /**
     * Returns to the main menu, stops all client/server connections, and closes opponent windows.
     */
    public void toTheMain() {
        closeOpponents();
        client.stop();
        if(server != null) {
            server.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }

    /**
     * Sets up and renders the local player's game board.
     */
    private void createGameBoard() {
        this.cellSize = FIELD_SIZE / boardSize;
        GridHelper.clearGameGrid(gameGrid);
        GridHelper.setupGridConstraints(boardSize, gameGrid, FIELD_SIZE);
        GridHelper.loadImages();
        GridHelper.createCells(game, gameGrid, cellSize, boardSize, this::handleCellClick);
    }

    /**
     * Handles a player's click on a game cell, updates the game state, and notifies the server.
     * @param node the clicked game node
     */
    private void handleCellClick(GameNode node) {
        game.setLastTurnedNode(node.getPosition());
        node.turn();
        updateStepsDisplay();
        client.sendTurn(node.getPosition());
        GridHelper.updateAfterClick(node, boardSize, game, gameGrid, cellSize, this::handleCellClick);
        if(game.checkWin()) {
            stopTimer();
            client.sendWin();
        }
    }

    /**
     * Initializes the game timer.
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
     * Starts the game timer and resets the counters.
     */
    private void startTimer() {
        secondsElapsed = 0;
        stepsTaken = 0;
        updateTimerDisplay();
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
     * Updates the timer label in the format mm:ss.
     */
    private void updateTimerDisplay() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
    }

    /**
     * Updates the step counter label.
     */
    private void updateStepsDisplay() {
        ++stepsTaken;
        stepsLabel.setText(String.format("Steps: %d/%d", stepsTaken, game.turnsToWin()));
    }

    /**
     * Closes all open windows showing opponents' games.
     */
    public void closeOpponents() {
        for (Stage stage : opponentStages) {
            stage.close();
        }
        opponentStages.clear();
    }
}
