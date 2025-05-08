package gui.controllers;

import game.Game;
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

public class ArchiveController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public VBox contentBox;
    private Game game;

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

    private void loadGameFromArchive(int gameId) throws IOException {
        FXMLLoader loader = new FXMLLoader(GridHelper.class.getResource("/fxml/archive_game.fxml"));
        Parent root = loader.load();

        GameArchiveController controller = loader.getController();
        controller.loadGame(gameId);
        controller.setPrimaryStage(primaryStage);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Archive Game");
    }

    @FXML public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
    }
}
