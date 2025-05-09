package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameClient;
import multiplayer.GameServer;
import java.util.List;

/**
 * Controller for the multiplayer victory dialog.
 * <p>
 * This controller is responsible for displaying the victory screen when a player wins the game.
 * It personalizes the message for the winning player and shows a general message for the others.
 * It also provides a method to return to the main menu, closing all related game windows and
 * stopping any running client/server processes.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class MultiplayerWinController {
    @FXML private Label PlayerWinId;
    private Stage primaryStage;
    private List<Stage> opponentsStages;
    private GameClient client;
    private GameServer server;
    private Stage dialogStage;

    /**
     * Sets the winner's ID and updates the dialog text accordingly.
     *
     * @param id the ID of the winning player
     */
    public void setWinnerId(int id) {
        if(id != client.getPlayerId())
            PlayerWinId.setText("Player " + id + " wins");
        else
            PlayerWinId.setText("You win!");
    }

    /**
     * Sets the primary stage and a list of stages for opponent windows.
     *
     * @param primaryStage     the main game window
     * @param opponents        list of opponent player stages to be closed on return
     */
    public void setStages(Stage primaryStage, List<Stage> opponents) {this.primaryStage = primaryStage; this.opponentsStages = opponents;}

    /**
     * Sets the multiplayer client instance.
     *
     * @param client the game client used for multiplayer communication
     */
    public void setClient(GameClient client) {this.client = client;}

    /**
     * Sets the multiplayer server instance (used only if this player was the host).
     *
     * @param server the game server instance
     */
    public void setServer(GameServer server) {this.server = server;}

    /**
     * Sets the stage for the victory dialog window.
     *
     * @param dialogStage the JavaFX stage displaying the victory dialog
     */
    public void setDialogStage(Stage dialogStage) {this.dialogStage = dialogStage;}

    /**
     * Handles the action of returning to the main menu.
     * <p>
     * Closes all opponent stages, stops the client and server (if applicable),
     * and loads the main menu scene.
     * </p>
     */
    public void toTheMain() {
        for (Stage stage : opponentsStages) {
            stage.close();
        }
        opponentsStages.clear();
        client.stop();
        if(server != null) {
            server.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
        if (dialogStage != null) dialogStage.close();
    }
}

