/**
 * Represents a single node (tile) on the game board.
 * A node can be a power source, bulb, or connecting wire, and contains connectors on its sides.
 * <p>
 * Supports rotation, lighting logic, and is observable via the {@link AbstractObservableField} class.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 * @author Olha Tomylko (xtomylo00)
 * @author Alina Paliienko (xpaliia00)
 */

package common;

import ija.ija2024.tool.common.AbstractObservableField;
import java.util.ArrayList;
import java.util.List;

public class GameNode extends AbstractObservableField {
    private boolean bulb;
    private boolean power;
    private boolean link;
    private boolean lit;
    private final Position position;
    private boolean[] connectors;

    private int correctRotation = 0;  // How many times the node needs to be rotated to reach the solved position

    private int currentRotation = 0;  // How many times the node has been rotated


    /**
     * Constructs a new GameNode at the specified position.
     *
     * @param position the position of the node on the game board
     */
    public GameNode(Position position) {
        this.position = position;
        this.connectors = new boolean[]{false, false, false, false};
        this.lit = false;
    }

    /**
     * Sets this node as a bulb with a connector on the given side.
     *
     * @param side the side where the bulb is connected
     */
    public void setBulb(Side side) {
        this.bulb = true;
        setConnectors(side);
    }

    /**
     * Sets this node as a power source with connectors on the given sides.
     *
     * @param sides the sides to set connectors on
     */
    public void setPower(Side... sides) {
        this.power = true;
        setConnectors(sides);
    }

    /**
     * Sets this node as a link with connectors on the given sides.
     *
     * @param sides the sides to set connectors on
     */
    public void setLink(Side... sides) {
        this.link = true;
        setConnectors(sides);
    }

    /**
     * Sets connectors on the specified sides.
     *
     * @param sides the sides to set connectors on
     */
    private void setConnectors(Side... sides){
        for (Side side : sides){
            this.connectors[side.ordinal()] = true;
        }
    }

    /**
     * Removes the connector from the specified side.
     *
     * @param side the side to remove the connector from
     */
    public void deleteConnector(Side side) {
        this.connectors[side.ordinal()] = false;
    }

    /**
     * Sets the number of correct clockwise turns needed to solve the node.
     *
     * @param turns number of turns modulo 4
     */
    public void setCorrectRotation(int turns) {
        this.correctRotation = turns % 4;
    }

    /**
     * Resets the current rotation counter to zero.
     */
    public void resetCurrentRotation() {
        this.currentRotation = 0;
    }

    /**
     * Returns how many clockwise turns are needed to return to the solved position.
     *
     * @return the number of necessary rotations (0–3)
     */
    public int getHint() {
        int delta = (4 + currentRotation - correctRotation) % 4;

        if (isCross()) {
            return 0; // No orientation matters
        }
        if (isLong()) {
            return (delta % 2 == 0) ? 0 : 1; // I-shape has 180° symmetry
        }
        // For corner (L-shape) or half-cross (T-shape)
        return (4 - delta) % 4;
    }

    /**
     * Rotates the node 90 degrees clockwise.
     * Updates connector directions and notifies observers.
     */
    public void turn() {
        boolean[] rotated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            rotated[(i + 3) % 4] = connectors[i];
        }
        this.connectors = rotated;

        currentRotation = (currentRotation + 1) % 4;

        notifyObservers();
    }

    /**
     * Rotates the node 90 degrees counter-clockwise.
     * Updates connector directions and notifies observers.
     */
    public void turnBack() {
        boolean[] rotated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            rotated[(i + 1) % 4] = connectors[i];
        }
        this.connectors = rotated;
        notifyObservers();
    }

    /**
     * Sets whether this node is currently lit.
     *
     * @param lit true if lit, false otherwise
     */
    public void setLit(boolean lit) {
        this.lit = lit;
    }

    /**
     * Checks if this node has all four connectors (i.e., forms a cross).
     *
     * @return true if node is a cross, false otherwise
     */
    public boolean isCross(){
        return north() && south() && east() && west();
    }

    /**
     * Checks if this node has three connectors in a T-shape.
     *
     * @return true if half-cross (T-link), false otherwise
     */
    public boolean isHalfCross() {
        return (north() && south() && (east() ^ west())) ||
                (east() && west() && (north() ^ south()));
    }

    /**
     * Checks if this node has two connectors in an L-shape.
     *
     * @return true if corner (L-link), false otherwise
     */
    public boolean isCorner() {
        return (north() && east() && !south() && !west()) ||
                (north() && west() && !south() && !east()) ||
                (south() && east() && !north() && !west()) ||
                (south() && west() && !north() && !east());
    }

    /**
     * Checks if this node has two connectors in a straight line.
     *
     * @return true if long (I-link), false otherwise
     */
    public boolean isLong(){
        return (north() && south() && !east() && !west()) ||
                (east() && west() && !north() && !south());
    }

    @Override public boolean north() {return this.connectors[Side.NORTH.ordinal()];}
    @Override public boolean south() {return this.connectors[Side.SOUTH.ordinal()];}
    @Override public boolean east() {return this.connectors[Side.EAST.ordinal()];}
    @Override public boolean west() {return this.connectors[Side.WEST.ordinal()];}
    @Override public boolean light() {return this.lit;}
    @Override public boolean isBulb() {return this.bulb;}
    @Override public boolean isPower() {return this.power;}
    @Override public boolean isLink() {return this.link;}

    /**
     * Returns the position of this node.
     *
     * @return the node's position
     */
    public Position getPosition() {return this.position;}

    /**
     * Checks if this node has a connector on the specified side.
     *
     * @param side the side to check
     * @return true if a connector exists, false otherwise
     */
    public boolean containsConnector(Side side) {return this.connectors[side.ordinal()];}

    /**
     * Returns a list of sides where this node has connectors.
     *
     * @return a list of connected sides
     */
    public List<Side> getConnectors() {
        List<Side> list = new ArrayList<>();
        for (Side s : Side.values()) {
            if (this.containsConnector(s)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Returns a string representation of the node for debugging.
     *
     * @return formatted string showing type, position, and connectors
     */
    @Override
    public String toString() {
        String type;
        if (this.isPower()) type = "P";
        else if (this.isBulb()) type = "B";
        else if (this.isLink()) type = "L";
        else type = "E";

        StringBuilder connectorsStr = new StringBuilder();
        Side[] order = {Side.NORTH, Side.EAST, Side.SOUTH, Side.WEST};
        boolean first = true;
        for (Side side : order) {
            if (this.containsConnector(side)) {
                if (!first) connectorsStr.append(",");
                connectorsStr.append(side.name());
                first = false;
            }
        }

        return "{" + type + "[" + position.getRow() + "@" + position.getCol() + "][" + connectorsStr + "]}";
    }

    public GameNode copy() {
        GameNode clone = new GameNode(
                new Position(getPosition().getRow(),
                        getPosition().getCol()));
        if (isPower()) {
            clone.setPower(getConnectors().toArray(new Side[0]));
        } else if (isBulb()) {
            clone.setBulb(getConnectors().getFirst());
        } else if (!getConnectors().isEmpty()) {
            clone.setLink(getConnectors().toArray(new Side[0]));
        }

        clone.setLit(light());

        return clone;
    }
}
