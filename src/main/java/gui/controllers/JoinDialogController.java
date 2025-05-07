package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JoinDialogController {
    public Button joinButton;
    public TextField ipEnter;
    public TextField portEnter;
    public Button closeButton;

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void joinGame() {
    }

    public void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
