/**
 * Listener interface for receiving a callback when a player wins the game.
 *
 * <p>Useful in multiplayer scenarios to display victory messages or trigger
 * post-game logic such as disabling the board or showing a summary screen.</p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
package gui.controllers;

public interface GameWinListener {

    /**
     * Called when a player wins the game.
     *
     * @param winnerId the ID of the player who won
     */
    void onGameWin(int winnerId);
}
