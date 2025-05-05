package gui.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import json.GameArchive;

import java.io.IOException;

public class ArchiveController {
    private Stage primaryStage;
    public void setPrimaryStage(Stage primaryStage) {this.primaryStage = primaryStage;}

    public VBox contentBox;

    public void showGames() {
        contentBox.getChildren().clear();

        for (Integer gameId : GameArchive.listSavedGameIds()) {
            Button gameButton = new Button("Game " + gameId);
            gameButton.setStyle("""
                        -fx-background-color: transparent;
                        -fx-text-fill: white;
                        -fx-font-size: 26;
                        -fx-cursor: hand;
                    """);

            gameButton.setOnAction(e -> {
                System.out.println("Selected Game ID: " + gameId);
            });

            contentBox.getChildren().add(gameButton);
        }
    }

    public void toTheMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_menu.fxml"));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setTitle("Light Bulb Game");
        } catch (IOException e) {
            System.err.println("Error loading main menu: " + e.getMessage());
        }
    }
}
