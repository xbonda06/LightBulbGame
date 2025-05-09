package gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameClient;

/**
 * Controller for the client-side waiting screen in multiplayer mode.
 * <p>
 * This controller is responsible for displaying the current player count,
 * listening for the game start event from the server, and transitioning
 * the user to the multiplayer game screen when the game begins.
 * </p>
 *
 * <p>
 * It also provides a way to return to the main menu and handles resource cleanup
 * when the window is closed or when the game client is stopped.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MultiplayerWaitController implements GameStartListener, GamePlayerCountListener{
    @FXML public Label playerCount;
    private Stage primaryStage;
    private GameClient client;


    /**
     * Sets the primary stage used to switch between scenes.
     *
     * @param primaryStage the main JavaFX window
     */
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    /**
     * Sets the game client and registers this controller as a listener
     * for game start and player count change events.
     *
     * @param gameClient the client used to communicate with the game server
     */
    public void setGameClient(GameClient gameClient) {
        this.client = gameClient;
        this.client.setGameStartListener(this);
        this.client.setPlayerCountListener(this);
    }

    /**
     * Returns to the main menu and stops the multiplayer client.
     */
    public void toTheMain() {
        if (client != null) {
            client.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }

    /**
     * Called when the server notifies that the game has started.
     * Transitions to the multiplayer game screen.
     */
    @Override
    public void onGameStarted() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/multiplayer_main.fxml"));
                Parent root = loader.load();

                MultiplayerGameController controller = loader.getController();
                controller.setServer(null);
                controller.setClient(client);
                controller.setPrimaryStage(primaryStage);
                controller.showGame();

                primaryStage.setScene(new Scene(root, 800, 600));
                primaryStage.setTitle("Light Bulb Game - ");
                primaryStage.setOnCloseRequest(e -> {
                    controller.closeOpponents();
                    closeScene();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Updates the player count label when the number of connected players changes.
     *
     * @param count the number of connected players
     */
    @Override
    public void onPlayerCountChanged(int count) {
        Platform.runLater(() -> {
            playerCount.setText("Players: " + count);
        });
    }

    /**
     * Cleans up resources and closes the application window.
     */
    private void closeScene() {
        client.stop();
        primaryStage.close();
    }
}
