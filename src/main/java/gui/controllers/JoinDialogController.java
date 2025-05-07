package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class JoinDialogController {
    public Button joinButton;
    public TextField ipEnter;
    public TextField portEnter;
    public Button closeButton;
    private Stage dialogStage;
    private MultiplayerController multiplayerController;

    public void setMultiplayerController(MultiplayerController controller) {
        this.multiplayerController = controller;
    }

    public String getIp() {
        return ipEnter.getText();
    }

    public int getPort() {
        try {
            return Integer.parseInt(portEnter.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void joinGame() {
        String ip = getIp();
        int port = getPort();

        boolean hasError = false;

        ipEnter.setStyle("");
        portEnter.setStyle("");

        if (port < 1000 || port > 9999) {
            portEnter.setStyle("-fx-border-color: red;");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        try {
            multiplayerController.startClient(ip, port);
            if (dialogStage != null) dialogStage.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ipEnter.setStyle("-fx-border-color: red;");
            portEnter.setStyle("-fx-border-color: red;");
        }
    }


    public void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
