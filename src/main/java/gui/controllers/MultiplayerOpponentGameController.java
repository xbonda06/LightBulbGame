/*
 * Author: Olha Tomylko (xtomylo00)
 *
 * Description:
 */

package gui.controllers;

import game.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class MultiplayerOpponentGameController {
    @FXML public StackPane rootPane;
    @FXML public Label playerId;
    @FXML public GridPane gameGrid;
    private Game game;

    public void setGame(Game opponentGame) {this.game = opponentGame;}
}
