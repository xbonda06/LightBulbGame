package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import multiplayer.GameServer;
import java.io.IOException;
import java.util.Random;

public class MultiplayerController {
    public Label port;
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    @FXML public Button undoButton;
    @FXML public Button redoButton;
    @FXML public Label stepsLabel;
    @FXML public Label timerLabel;
    @FXML public Button createButton;
    @FXML public Button joinGame;
    @FXML public Button mainButton;
    @FXML public Button startButton;
    @FXML public Label ipAddress;
    @FXML public Label playerCount;

    // From the game mode -> multiplayer_main.fxml

    @FXML public void toMainMenu() {
    }

    @FXML public void getRedo() {
    }

    @FXML public void getUndo() {
    }

    // From the menu when choosing CREATE or JOIN -> multiplayer_menu.fxml

    @FXML
    public void createGame() throws IOException {
        int portNumber = new Random().nextInt(9000) + 1000;
        int difficulty = 5;

        GameServer server = new GameServer(portNumber, difficulty);
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/wait_for_connection.fxml"));
        Parent root = loader.load();

        MultiplayerController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        controller.port.setText("Port: " + portNumber);
        controller.ipAddress.setText("Server IP: " + server.getIpAddress());

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Light Bulb Game - Multiplayer");
    }


    @FXML public void joinGame() {
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

    private void joinGameSend() {

    }

    @FXML public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
    }

    // From waiting screen -> wait_for_connection.fxml

    @FXML public void startGame() {
    }
}

//NEW GAME:
//instance Server. Port random
//instance client + connect

//JOIN GAME:
//IP and Port
//instance client

