package gui.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import multiplayer.GameClient;
import multiplayer.GameServer;
import java.io.IOException;

/**
 * Controller for the multiplayer connection waiting screen.
 * <p>
 * This controller displays the connection status for a multiplayer session,
 * including IP address, port number, and connected player count.
 * It allows the host to start the game once at least two players are connected
 * or to return to the main menu.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MultiplayerConnectionController implements GamePlayerCountListener {
    @FXML public Button startButton;
    @FXML public Label ipAddress;
    @FXML public Label playerCount;
    @FXML public Label port;
    private Stage primaryStage;
    private GameServer server;
    private GameClient client;
    private int players = 1;

    /**
     * Sets the primary stage of the application.
     *
     * @param primaryStage the primary stage window
     */
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    /**
     * Sets the game server instance.
     *
     * @param server the game server
     */
    public void setServer(GameServer server) {this.server = server;}


    /**
     * Sets the game client instance and registers a player count listener.
     *
     * @param client the game client
     */
    public void setClient(GameClient client) {
        this.client = client;
        this.client.setPlayerCountListener(this);
    }

    /**
     * Returns to the main menu and stops the server.
     */
    @FXML public void toTheMain() {
        server.stop();
        GridHelper.loadMainMenu(primaryStage);
    }

    /**
     * Starts the multiplayer game if at least two players are connected.
     * Otherwise, highlights the warning with red styles for a short period.
     *
     * @throws IOException if the multiplayer game scene fails to load
     */
    public void startGame() throws IOException {
        if (players > 1) {
            FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/multiplayer_main.fxml"));
            Parent root = loader.load();

            MultiplayerGameController controller = loader.getController();
            controller.setServer(server);
            controller.setClient(client);
            controller.setPrimaryStage(primaryStage);
            client.sendStartGame();
            controller.showGame();

            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setTitle("Light Bulb Game - ");
            primaryStage.setOnCloseRequest(e -> {
                controller.closeOpponents();
                closeScene(server);
            });
        }
        else {
            startButton.setStyle("-fx-background-color: red");
            playerCount.setStyle("-fx-text-fill: red");

            PauseTransition resetStyle = new PauseTransition(Duration.seconds(1));
            resetStyle.setOnFinished(ev -> {
                startButton.setStyle("");
                playerCount.setStyle("");
            });
            resetStyle.play();
        }
    }

    /**
     * Callback method triggered when the number of connected players changes.
     *
     * @param count the new number of connected players
     */
    @Override
    public void onPlayerCountChanged(int count) {
        Platform.runLater(() -> {
            playerCount.setText("Players: " + count);
            this.players = count;
        });
    }


    /**
     * Closes the current scene and stops the server.
     *
     * @param server the game server to stop
     */
    private void closeScene(GameServer server){
        server.stop();
        primaryStage.close();
    }
}
