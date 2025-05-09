package gui.controllers;

/**
 * Listener interface for receiving notifications when the number of connected players changes.
 *
 * <p>Implement this interface if you need to react to changes in the number of players,
 * for example, in a multiplayer lobby or game status panel.</p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
public interface GamePlayerCountListener {

    /**
     * Called when the number of connected players changes.
     *
     * @param count the current number of connected players
     */
    void onPlayerCountChanged(int count);
}
