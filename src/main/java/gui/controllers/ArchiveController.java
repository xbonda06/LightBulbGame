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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArchiveController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}
    public VBox contentBox;
    private Game game;

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

            Button gameButton = getButton(gameId);

            contentBox.getChildren().add(gameButton);
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
