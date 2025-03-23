package common;

public class GameNode {
    private boolean bulb;
    private boolean power;
    private boolean link;
    private final Position position;
    private boolean[] connectors;

    public GameNode(Position position) {
        this.position = position;
        this.connectors = new boolean[]{false, false, false, false};
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
    }

    public boolean isBulb() {
        return this.bulb;
    }

    public boolean isPower() {
        return this.power;
    }

    public boolean isLink() {
        return this.link;
    }

    public Position getPosition() {
        return this.position;
    }

    public boolean containsConnector(Side side) {
        return this.connectors[side.ordinal()];
    }
}
