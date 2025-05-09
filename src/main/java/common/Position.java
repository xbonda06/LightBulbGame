/**
 * Represents a coordinate on the game board using row and column indices.
 * <p>
 * This class is immutable and used to identify the position of nodes (fields) in the grid.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
package common;

public class Position {

    private final int row;
    private final int col;

    /**
     * Constructs a new position with the given row and column.
     *
     * @param row the row number (1-based index)
     * @param col the column number (1-based index)
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row index of this position.
     *
     * @return the row index
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Returns the column index of this position.
     *
     * @return the column index
     */
    public int getCol() {
        return this.col;
    }

    /**
     * Compares this position to another object for equality.
     *
     * @param obj the object to compare with
     * @return true if the other object is a Position with the same row and column, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Position && this.row == ((Position) obj).row && this.col == ((Position) obj).col;
    }
}