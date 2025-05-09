package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import json.GameArchive;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller responsible for managing the archive menu UI.
 * <p>
 * It displays a list of previously played games, grouped by date (from newest to oldest),
 * and allows the user to select a game to view its progress in a read-only mode.
 * </p>
 *
 * @author Olha Tomylko (xtomylo00)
 */
public class ArchiveController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public VBox contentBox;

    /**
     * Loads and displays all archived games grouped by date.
     * <p>
     * For each date, a header is created, followed by a list of buttons representing individual games.
     * Games and dates are sorted in descending (newest-first) order.
     * </p>
     */
    public void showGames() {
        contentBox.getChildren().clear();

        Map<LocalDate, List<Integer>> grouped = new HashMap<>();
        for (Integer gameId : GameArchive.listSavedGameIds()) {
            LocalDate date = GameArchive.getGameDate(gameId);
            if (date == null) continue;

            grouped.computeIfAbsent(date, d -> new ArrayList<>()).add(gameId);
        }

        List<LocalDate> sortedDates = new ArrayList<>(grouped.keySet());
        sortedDates.sort(Comparator.reverseOrder());

        for (LocalDate date : sortedDates) {
            Label label = new Label(date.toString());
            label.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #FFD900;
            -fx-font-size: 23;
        """);
            contentBox.getChildren().add(label);

            List<Integer> ids = grouped.get(date);
            ids.sort(Comparator.reverseOrder());

            for (Integer gameId : ids) {
                Button gameButton = getButton(gameId);
                contentBox.getChildren().add(gameButton);
            }
        }
    }

    /**
     * Creates a button for the given game ID.
     * When clicked, the button opens the selected game in archive view mode.
     *
     * @param gameId the ID of the archived game
     * @return a styled Button instance associated with the game ID
     */
    private Button getButton(Integer gameId) {
        Button gameButton = new Button("Game " + gameId);
        gameButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 20;
                    -fx-cursor: hand;
                """);

        gameButton.setOnAction(e -> {
            try {
                loadGameFromArchive(gameId);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return gameButton;
    }

    /**
     * Loads the archived game with the specified ID and switches the scene to archive view.
     *
     * @param gameId the ID of the archived game to load
     * @throws IOException if the FXML file for archive view cannot be loaded
     */
    private void loadGameFromArchive(int gameId) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/archive_game.fxml"));
        Parent root = loader.load();

        GameArchiveController controller = loader.getController();
        controller.loadGame(gameId);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Archive Game");
    }

    /**
     * Handles the action of returning to the main menu.
     * Triggered by the corresponding FXML button.
     */
    @FXML public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
    }
}
