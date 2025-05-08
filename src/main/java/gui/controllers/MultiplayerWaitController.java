/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * Controller for the client-side waiting screen in multiplayer mode.
 * Waits for the game to start and transitions to the main multiplayer game view once it begins.
 */

package gui.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import multiplayer.GameClient;

import java.io.IOException;

public class MultiplayerWaitController implements GameStartListener{
    private Stage primaryStage;
    private GameClient client;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public void setGameClient(GameClient gameClient) {
        this.client = gameClient;
        gameClient.setGameStartListener(this);
    }

    public void toTheMain() {
        if (client != null) {
            client.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }

    @Override
    public void onGameStarted() throws IOException {
        // TODO: Load the multiplayer game view

        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/multiplayer_main.fxml"));
        Parent root = loader.load();

        MultiplayerGameController controller = loader.getController();
        controller.setServer(null);
        controller.setClient(client);
        controller.setPrimaryStage(primaryStage);
        client.sendStartGame();
        controller.showGame();

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - ");
        primaryStage.setOnCloseRequest(e -> {
            controller.closeOpponents();
            closeScene();
        });
    }

    private void closeScene() {
        client.stop();
        primaryStage.close();
    }
}
