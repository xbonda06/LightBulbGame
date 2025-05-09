/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * Controller for the client-side waiting screen in multiplayer mode.
 * Waits for the game to start and transitions to the main multiplayer game view once it begins.
 */

package gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameClient;

public class MultiplayerWaitController implements GameStartListener, GamePlayerCountListener{
    @FXML public Label playerCount;
    private Stage primaryStage;
    private GameClient client;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public void setGameClient(GameClient gameClient) {
        this.client = gameClient;
        this.client.setGameStartListener(this);
        this.client.setPlayerCountListener(this);
    }

    public void toTheMain() {
        if (client != null) {
            client.stop();
        }
        GridHelper.loadMainMenu(primaryStage);
    }

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

    @Override
    public void onPlayerCountChanged(int count) {
        Platform.runLater(() -> {
            playerCount.setText("Players: " + count);
        });
    }

    private void closeScene() {
        client.stop();
        primaryStage.close();
    }
}
