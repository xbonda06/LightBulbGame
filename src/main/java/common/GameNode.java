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


    public GameNode(Position position) {
        this.position = position;
        this.connectors = new boolean[]{false, false, false, false};
        this.lit = false;
    }

    public void setBulb(Side side) {
        this.bulb = true;
        setConnectors(side);
    }

    public void setPower(Side... sides) {
        this.power = true;
        setConnectors(sides);
    }

    public void setLink(Side... sides) {
        this.link = true;
        setConnectors(sides);
    }

    private void setConnectors(Side... sides){
        for (Side side : sides){
            this.connectors[side.ordinal()] = true;
        }
    }

    public void setCorrectRotation(int turns) {
        this.correctRotation = turns % 4;
    }

    public void resetCurrentRotation() {
        this.currentRotation = 0;
    }

    /// Returns the number of turns needed to reach the correct rotation
    public int getHint() {
        return (4 - (currentRotation - correctRotation + 4) % 4) % 4;
    }

    public void turn() {
        boolean[] rotated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            rotated[(i + 3) % 4] = connectors[i];
        }
        this.connectors = rotated;

        currentRotation = (currentRotation + 1) % 4;

        notifyObservers();
    }

    public void turnBack() {
        boolean[] rotated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            rotated[(i + 1) % 4] = connectors[i];
        }
        this.connectors = rotated;
        notifyObservers();
    }


    public void setLit(boolean lit) {
        this.lit = lit;
    }

    public boolean isCross(){
        return north() && south() && east() && west();
    }

    public boolean isHalfCross() {
        return (north() && south() && (east() ^ west())) ||
                (east() && west() && (north() ^ south()));
    }

    public boolean isCorner() {
        return (north() && east() && !south() && !west()) ||
                (north() && west() && !south() && !east()) ||
                (south() && east() && !north() && !west()) ||
                (south() && west() && !north() && !east());
    }

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
    public Position getPosition() {return this.position;}
    public boolean containsConnector(Side side) {return this.connectors[side.ordinal()];}

    /** Returns a list of sides that have connectors on this node. */
    public List<Side> getConnectors() {
        List<Side> list = new ArrayList<>();
        for (Side s : Side.values()) {
            if (this.containsConnector(s)) {
                list.add(s);
            }
        }
        return list;
    }

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
