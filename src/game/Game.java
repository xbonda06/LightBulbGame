package game;

import ija.ija2024.homework1.common.GameNode;
import ija.ija2024.homework1.common.Position;
import ija.ija2024.homework1.common.Side;

public class Game {
    private final int rows;
    private final int cols;
    private final GameNode[][] nodes;
    private boolean isPower = false;

    private Game(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.nodes = new GameNode[rows][cols];
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                this.nodes[r - 1][c - 1] = new GameNode(new Position(r, c));
            }
        }
    }

    public static Game create(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Invalid game size.");
        }
        return new Game(rows, cols);
    }

    public int rows() {
        return this.rows;
    }

    public int cols() {
        return this.cols;
    }

    public GameNode node(Position p) {
        return this.nodes[p.getRow() - 1][p.getCol() - 1];
    }

    public GameNode createBulbNode(Position p, Side s) {
        if (p.getRow() < 1 || p.getRow() > this.rows || p.getCol() < 1 || p.getCol() > this.cols) {
            return null;
        }

        GameNode node = this.node(p);
        node.setBulb(s);
        nodes[p.getRow() - 1][p.getCol() - 1] = node;
        return node;
    }

    public GameNode createPowerNode(Position p, Side... sides) {
        if (p.getRow() < 1 || p.getRow() >= this.rows || p.getCol() < 1 || p.getCol() >= this.cols) {
            return null;
        }

        if (sides.length == 0 || sides.length > 4){
            return null;
        }

        if (this.isPower){
            return null;
        }

        GameNode node = this.node(p);
        node.setPower(sides);
        nodes[p.getRow() - 1][p.getCol() - 1] = node;
        this.isPower = true;
        return node;
    }

    public GameNode createLinkNode (Position p, Side...sides){
        if (p.getRow() < 1 || p.getRow() > this.rows || p.getCol() < 1 || p.getCol() > this.cols) {
            return null;
        }

        if (sides.length < 2 || sides.length > 4){
            return null;
        }

        GameNode node = this.node(p);
        node.setLink(sides);
        nodes[p.getRow() - 1][p.getCol() - 1] = node;
        return node;
    }
}