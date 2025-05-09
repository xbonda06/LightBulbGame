/**
 * Controller for the Join Game dialog in multiplayer mode.
 * <p>
 * This controller handles user input for IP address and port, validates the input,
 * and attempts to connect to a multiplayer server. It also manages the closing
 * of the dialog window, either after a successful connection or by user action.
 * </p>
 *
 * Features:
 * <ul>
 *     <li>Collects and validates IP address and port input from the user.</li>
 *     <li>Initiates a connection to the server via the MultiplayerController.</li>
 *     <li>Closes the dialog window on success or user cancellation.</li>
 * </ul>
 *
 * @author Olha Tomylko
 */
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


    /**
     * Sets the MultiplayerController used to initiate a client connection.
     *
     * @param controller the instance of MultiplayerController to use
     */
    public void setMultiplayerController(MultiplayerController controller) {
        this.multiplayerController = controller;
    }

    /**
     * Retrieves the IP address entered by the user.
     *
     * @return a {@code String} representing the IP address
     */
    public String getIp() {
        return ipEnter.getText();
    }


    /**
     * Retrieves the port number entered by the user.
     * <p>
     * Returns -1 if the input is not a valid integer.
     * </p>
     *
     * @return an integer representing the port, or -1 if invalid
     */
    public int getPort() {
        try {
            return Integer.parseInt(portEnter.getText());
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    /**
     * Sets the current dialog stage so it can be closed later.
     *
     * @param stage the dialog {@code Stage} instance
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Attempts to connect to the multiplayer server using user-provided IP and port.
     * <p>
     * If the port is invalid or the connection fails, it highlights the fields in red.
     * On successful connection, the dialog window is closed.
     * </p>
     */
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

    /**
     * Closes the join game dialog window.
     */
    public void closeDialog() {
        if (dialogStage != null) dialogStage.close();
    }
}
