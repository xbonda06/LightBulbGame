
package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameClient;
import multiplayer.GameServer;

import java.util.List;

public class MultiplayerWinController {
    @FXML private Label PlayerWinId;
    private Stage primaryStage;
    private List<Stage> opponentsStages;
    private GameClient client;
    private GameServer server;
    private Stage dialogStage;

    public void setWinnerId(int id) {
        PlayerWinId.setText("Player " + id + " wins");
    }
    public void setStages(Stage primaryStage, List<Stage> opponents) {this.primaryStage = primaryStage; this.opponentsStages = opponents;}
    public void setClient(GameClient client) {this.client = client;}
    public void setServer(GameServer server) {this.server = server;}
    public void setDialogStage(Stage dialogStage) {this.dialogStage = dialogStage;}

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

