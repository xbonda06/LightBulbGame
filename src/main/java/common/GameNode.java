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

    public void turn() {
        boolean[] rotated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            rotated[(i + 3) % 4] = connectors[i];
        }
        this.connectors = rotated;
        notifyObservers();
    }

    public void setLit(boolean lit) {
        this.lit = lit;
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

    /** Getter for the `lit` flag used by copy(). */
    public boolean isLit() {
        return this.lit;
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
            clone.setBulb(getConnectors().get(0));   // у лампочки ровно 1 коннектор
        } else if (!getConnectors().isEmpty()) {
            clone.setLink(getConnectors().toArray(new Side[0]));
        }

        clone.setLit(isLit());

        return clone;
    }
}
