/**
 * Controller for handling multiplayer menu interactions.
 * <p>
 * This controller manages the multiplayer menu where users can choose to create or join a game.
 * It handles transitions between scenes and sets up both the server and client sides of the game connection.
 * </p>
 *
 * Features:
 * <ul>
 *     <li>Creates a game server and starts it in a background thread.</li>
 *     <li>Starts a client for connecting to either local or remote multiplayer servers.</li>
 *     <li>Opens dialog for IP and port entry when joining a game.</li>
 *     <li>Manages scene transitions to the waiting or main multiplayer views.</li>
 * </ul>
 *
 * @author Olha Tomylko (xtomylo00)
 */

package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import multiplayer.GameClient;
import multiplayer.GameServer;

import java.io.IOException;
import java.util.Random;

public class MultiplayerController {

    private Stage primaryStage;

    /**
     * Sets the main application stage.
     *
     * @param primaryStage the main application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML public StackPane rootPane;
    @FXML public GridPane gameGrid;
    @FXML public Button createButton;
    @FXML public Button joinGame;
    @FXML public Button mainButton;

    /**
     * Called when the user chooses to create a new multiplayer game.
     * Initializes a server and client, sets up the waiting screen.
     *
     * @throws IOException if the FXML scene cannot be loaded
     */
    @FXML
    public void createGame() throws IOException {
        int portNumber = new Random().nextInt(9000) + 1000;
        int difficulty = 5;

        GameServer server = new GameServer(portNumber, difficulty);
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        GameClient client = new GameClient(server.getIpAddress(), portNumber);
        client.start();

        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/wait_server.fxml"));
        Parent root = loader.load();

        MultiplayerConnectionController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        controller.setClient(client);
        controller.port.setText("Port: " + portNumber);
        controller.ipAddress.setText("Server IP: " + server.getIpAddress());
        controller.setServer(server);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
        primaryStage.setOnCloseRequest(e -> closeScene(server));
    }

    /**
     * Gracefully stops the server and closes the application stage.
     *
     * @param server the running game server
     */
    private void closeScene(GameServer server) {
        server.stop();
        primaryStage.close();
    }

    /**
     * Called when the user chooses to join an existing multiplayer game.
     * Opens a dialog for entering the IP and port of the server.
     */
    @FXML
    public void joinGame() {
        try {
            Rectangle overlay = new Rectangle(rootPane.getWidth(), rootPane.getHeight(), Color.rgb(0, 0, 0, 0.7));
            overlay.widthProperty().bind(rootPane.widthProperty());
            overlay.heightProperty().bind(rootPane.heightProperty());
            rootPane.getChildren().add(overlay);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/join_dialog.fxml"));
            Parent root = loader.load();

            JoinDialogController controller = loader.getController();

            Stage dialogStage = new Stage(StageStyle.UNDECORATED);
            controller.setDialogStage(dialogStage);
            controller.setMultiplayerController(this);

            dialogStage.setOnHidden(e -> {
                rootPane.getChildren().remove(overlay);
                String ip = controller.getIp();
                int port = controller.getPort();
                System.out.println("User entered IP: " + ip + ", port: " + port);
            });

            GridHelper.openDialog(dialogStage, root, primaryStage);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Starts the multiplayer client and switches to the waiting screen.
     *
     * @param ip   the IP address of the server
     * @param port the port number to connect to
     * @throws IOException if loading the FXML scene fails
     */
    public void startClient(String ip, int port) throws IOException {
        GameClient client = new GameClient(ip, port);
        client.start();

        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/wait_client.fxml"));
        Parent root = loader.load();

        MultiplayerWaitController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        controller.setGameClient(client);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
        primaryStage.setOnCloseRequest(e -> closeClient(client));
    }

    /**
     * Gracefully stops the client and closes the application stage.
     *
     * @param client the running game client
     */
    private void closeClient(GameClient client) {
        client.stop();
        primaryStage.close();
    }

    /**
     * Returns to the main menu from the multiplayer menu.
     */
    @FXML
    public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
    }
}