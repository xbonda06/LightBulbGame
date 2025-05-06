package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import json.GameArchive;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArchiveController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public VBox contentBox;

    public void showGames() {
        contentBox.getChildren().clear();

        List<Integer> gameIds = new ArrayList<>(GameArchive.listSavedGameIds());
        gameIds.sort(Comparator.reverseOrder());
        LocalDate lastDate = null;

        for (Integer gameId : gameIds) {
            LocalDate date = GameArchive.getGameDate(gameId);
            if (date == null) continue;

            if (!date.equals(lastDate)) {
                lastDate = date;

                Label label = new Label(date.toString());
                label.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: #FFD900;
                    -fx-font-size: 23;
                """);
                contentBox.getChildren().add(label);
            }

            Button gameButton = new Button("Game " + gameId);
            gameButton.setStyle("""
                        -fx-background-color: transparent;
                        -fx-text-fill: white;
                        -fx-font-size: 20;
                        -fx-cursor: hand;
                    """);

            gameButton.setOnAction(e -> System.out.println("Selected Game ID: " + gameId));

            contentBox.getChildren().add(gameButton);
        }
    }

    @FXML public void toTheMain() {
        GridHelper.loadMainMenu(primaryStage);
    }
}
