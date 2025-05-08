/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 * This controller manages the multiplayer connection waiting screen.
 * It displays connection details (IP address, port, and player count) and allows the user to either start the game
 * (which transitions to the multiplayer game window) or exit to the main menu.
 */

package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import multiplayer.GameClient;
import multiplayer.GameServer;
import java.io.IOException;

public class MultiplayerConnectionController {
    @FXML public Button startButton;
    @FXML public Label ipAddress;
    @FXML public Label playerCount;
    @FXML public Label port;
    private Stage primaryStage;
    private GameServer server;
    private GameClient client;

    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public void setServer(GameServer server) {this.server = server;}
    public void setClient(GameClient client) {this.client = client;}

    @FXML public void toTheMain() {
        server.stop();
        GridHelper.loadMainMenu(primaryStage);
    }

    public void startGame() throws IOException {
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
    }
}
