/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * Controller for the client-side waiting screen in multiplayer mode.
 * Waits for the game to start and transitions to the main multiplayer game view once it begins.
 */

package gui.controllers;

import javafx.stage.Stage;
import multiplayer.GameClient;

public class MultiplayerWaitController implements GameStartListener{
    private Stage primaryStage;
    private GameClient gameClient;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
        gameClient.setGameStartListener(this);
    }

    public void toTheMain() {
        if (gameClient != null) {
            gameClient.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }

    @Override
    public void onGameStarted() {
        // TODO: Load the multiplayer game view
    }
}
