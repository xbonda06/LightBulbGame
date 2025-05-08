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

public class MultiplayerWaitController {
    private Stage primaryStage;
    private GameClient gameClient;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public void setGameClient(GameClient gameClient) {this.gameClient = gameClient;}

    public void toTheMain() {
        if (gameClient != null) {
            gameClient.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }
}
