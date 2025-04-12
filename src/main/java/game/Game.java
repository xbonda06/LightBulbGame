package game;

import common.GameNode;
import common.Position;
import common.Side;
import ija.ija2024.tool.common.Observable;
import ija.ija2024.tool.common.ToolEnvironment;
import ija.ija2024.tool.common.ToolField;

public class Game implements ToolEnvironment, Observable.Observer {
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

    public void init() {
        GameNode powerNode = null;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (nodes[r][c].isPower()) {
                    powerNode = nodes[r][c];
                    break;
                }
            }
            if (powerNode != null) break;
        }
        if (powerNode == null) return;

        propagateLight(powerNode);
    }

    private void propagateLight(GameNode start) {
        boolean[][] visited = new boolean[rows][cols];
        dfs(start.getPosition(), visited);
    }

    private void dfs(Position pos, boolean[][] visited) {
        int r = pos.getRow() - 1;
        int c = pos.getCol() - 1;

        if (r < 0 || r >= rows || c < 0 || c >= cols || visited[r][c]) return;
        GameNode node = nodes[r][c];
        visited[r][c] = true;

        node.setLit(true);

        for (Side side : Side.values()) {
            if (!node.containsConnector(side)) continue;

            Position neighborPos = switch (side) {
                case NORTH -> new Position(pos.getRow() - 1, pos.getCol());
                case SOUTH -> new Position(pos.getRow() + 1, pos.getCol());
                case EAST  -> new Position(pos.getRow(), pos.getCol() + 1);
                case WEST  -> new Position(pos.getRow(), pos.getCol() - 1);
            };

            int nr = neighborPos.getRow() - 1;
            int nc = neighborPos.getCol() - 1;

            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

            GameNode neighbor = nodes[nr][nc];
            Side opposite = switch (side) {
                case NORTH -> Side.SOUTH;
                case SOUTH -> Side.NORTH;
                case EAST  -> Side.WEST;
                case WEST  -> Side.EAST;
            };

            if (neighbor.containsConnector(opposite)) {
                dfs(neighborPos, visited);
            }
        }
    }

    public void updatePowerPropagation() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                nodes[r][c].setLit(false);
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (nodes[r][c].isPower()) {
                    propagateLight(nodes[r][c]);
                    return;
                }
            }
        }
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
        node.addObserver(this);
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
        node.addObserver(this);
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
        node.addObserver(this);
        nodes[p.getRow() - 1][p.getCol() - 1] = node;
        return node;
    }

    @Override
    public ToolField fieldAt(int i, int i1) {
        if (i < 0 || i >= this.rows || i1 < 0 || i1 >= this.cols) {
            return null;
        }
        return this.nodes[i][i1];
    }

    @Override
    public void update(Observable observable) {
        updatePowerPropagation();
    }
}