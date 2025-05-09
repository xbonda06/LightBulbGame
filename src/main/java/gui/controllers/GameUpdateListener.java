package gui.controllers;

/**
 * Listener interface for receiving game update notifications.
 *
 * <p>Implement this interface to handle events that should occur when the game state changes,
 * such as re-rendering the UI or updating the scoreboard.</p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
public interface GameUpdateListener {

    /**
     * Called whenever the game state is updated (e.g., after a move).
     */
    void onGameUpdate();
}
