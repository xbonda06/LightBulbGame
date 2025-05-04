package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class VictoryDialogController {
    @FXML
    public Button YesButton;
    @FXML
    public Button NoButton;

    private Runnable onYesAction;
    private Runnable onNoAction;

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setOnYesAction(Runnable onYesAction) {
        this.onYesAction = onYesAction;
    }

    public void setOnNoAction(Runnable onNoAction) {
        this.onNoAction = onNoAction;
    }

    @FXML
    public void handleYesButton() {
        if (onYesAction != null) onYesAction.run();
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    public void handleNoButton() {
        if (onNoAction != null) onNoAction.run();
        if (dialogStage != null) dialogStage.close();
    }
}

