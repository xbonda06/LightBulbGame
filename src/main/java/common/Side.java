package common;

/**
 * Enum representing the four cardinal directions on the game board.
 * <p>
 * Used to define connector directions and neighbor relationships between nodes.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
public enum Side {
    /**
     * Right direction (positive column).
     */
    EAST,

    /**
     * Upward direction (negative row).
     */
    NORTH,

    /**
     * Left direction (negative column).
     */
    WEST,

    /**
     * Downward direction (positive row).
     */
    SOUTH
}
