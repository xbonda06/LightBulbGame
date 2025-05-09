/**
 * Listener interface for receiving a callback when the game starts.
 *
 * <p>Use this interface to perform actions (e.g., switching views, enabling controls)
 * when a multiplayer game session begins.</p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
package gui.controllers;

public interface GameStartListener {

    /**
     * Called when the game has officially started.
     */
    void onGameStarted();
}
