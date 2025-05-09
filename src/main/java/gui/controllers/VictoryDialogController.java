package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the victory dialog that is displayed when the player wins the game.
 * <p>
 * This dialog offers two options: to start a new game (Yes) or to return to the main menu (No).
 * The corresponding actions are executed via the provided callback functions set for each button.
 * </p>
 *
 * <p>
 * The dialog can be closed after the action is performed, allowing for seamless navigation
 * between game states.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class VictoryDialogController {
    @FXML
    public Button YesButton;
    @FXML
    public Button NoButton;

    private Runnable onYesAction;
    private Runnable onNoAction;
    private Stage dialogStage;

    /**
     * Sets the stage for the victory dialog window.
     *
     * @param stage the JavaFX stage that holds the victory dialog
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Sets the callback action to be executed when the "Yes" button is pressed.
     *
     * @param onYesAction the action to execute when the "Yes" button is clicked
     */
    public void setOnYesAction(Runnable onYesAction) {
        this.onYesAction = onYesAction;
    }

    /**
     * Sets the callback action to be executed when the "No" button is pressed.
     *
     * @param onNoAction the action to execute when the "No" button is clicked
     */
    public void setOnNoAction(Runnable onNoAction) {
        this.onNoAction = onNoAction;
    }

    /**
     * Handles the "Yes" button click event.
     * <p>
     * Executes the provided "Yes" action and closes the dialog window.
     * </p>
     */
    @FXML
    public void handleYesButton() {
        if (onYesAction != null) onYesAction.run();
        if (dialogStage != null) dialogStage.close();
    }

    /**
     * Handles the "No" button click event.
     * <p>
     * Executes the provided "No" action and closes the dialog window.
     * </p>
     */
    @FXML
    public void handleNoButton() {
        if (onNoAction != null) onNoAction.run();
        if (dialogStage != null) dialogStage.close();
    }
}

