package game;

import common.GameNode;
import common.Position;
import common.Side;
import ija.ija2024.tool.common.Observable;
import ija.ija2024.tool.common.ToolEnvironment;
import ija.ija2024.tool.common.ToolField;

import java.util.*;

public class Game implements ToolEnvironment, Observable.Observer {
    private final int rows;
    private final int cols;
    private final GameNode[][] nodes;
    private boolean isPower = false;

    private final Stack<GameState> undoStack = new Stack<>();
    private final Stack<GameState> redoStack = new Stack<>();

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
        if (p.getRow() < 1 || p.getRow() > this.rows || p.getCol() < 1 || p.getCol() > this.cols) {
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

    public static Game generate(int rows, int cols) {
        if (rows <= 0 || cols <= 0)
            throw new IllegalArgumentException("Invalid game size.");

        Game game = new Game(rows, cols);
        Random random = new Random();

        // Set power to random position
        Position powerPos = new Position(random.nextInt(rows) + 1, random.nextInt(cols) + 1);
        game.createPowerNode(powerPos, Side.NORTH); // Default direction - will be changed in generation process

        for (Side side : Side.values()) {
            Position neighbor = game.neighbor(powerPos, side);
            if (neighbor != null) {
                game.connectNodes(powerPos, neighbor, side);
            }
        }

        // Generate connection tree
        game.generateFullConnections(powerPos);

        game.init();

        return game;
    }

    private void generateFullConnections(Position start) {
        Random random = new Random();
        boolean[][] visited = new boolean[rows][cols];
        Stack<Position> stack = new Stack<>();
        stack.push(start);
        visited[start.getRow() - 1][start.getCol() - 1] = true;

        while (!stack.isEmpty()) {
            Position current = stack.peek();
            List<Side> sides = new ArrayList<>(Arrays.asList(Side.values()));
            Collections.shuffle(sides);

            boolean moved = false;
            for (Side side : sides) {
                Position neighbor = neighbor(current, side);
                if (neighbor != null && !visited[neighbor.getRow() - 1][neighbor.getCol() - 1]) {
                    connectNodes(current, neighbor, side);
                    visited[neighbor.getRow() - 1][neighbor.getCol() - 1] = true;
                    stack.push(neighbor);
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                stack.pop();
            }
        }

        // Set bulbs on leaves of the tree (cells with one connection)
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Position p = new Position(r, c);
                GameNode node = node(p);
                if (!node.isPower() && connectedSides(node).size() == 1) {
                    List<Side> sides = connectedSides(node);
                    createBulbNode(p, sides.getFirst());
                } else if (!node.isPower() && connectedSides(node).size() >= 2) {
                    createLinkNode(p, connectedSides(node).toArray(new Side[0]));
                }
            }
        }
    }

    public void randomizeRotations() {
        Random random = new Random();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                GameNode node = node(new Position(r, c));
                int turns = random.nextInt(4);
                for (int t = 0; t < turns; t++) {
                    node.turn();
                }
            }
        }
    }

    private Position neighbor(Position p, Side side) {
        int r = p.getRow(), c = p.getCol();
        return switch (side) {
            case NORTH -> (r > 1) ? new Position(r - 1, c) : null;
            case SOUTH -> (r < rows) ? new Position(r + 1, c) : null;
            case EAST -> (c < cols) ? new Position(r, c + 1) : null;
            case WEST -> (c > 1) ? new Position(r, c - 1) : null;
        };
    }

    private void connectNodes(Position from, Position to, Side direction) {
        Side opposite = switch (direction) {
            case NORTH -> Side.SOUTH;
            case SOUTH -> Side.NORTH;
            case EAST -> Side.WEST;
            case WEST -> Side.EAST;
        };

        GameNode fromNode = node(from);
        GameNode toNode = node(to);

        fromNode.setLink(direction);
        toNode.setLink(opposite);
    }

    private List<Side> connectedSides(GameNode node) {
        List<Side> sides = new ArrayList<>();
        for (Side side : Side.values()) {
            if (node.containsConnector(side)) {
                sides.add(side);
            }
        }
        return sides;
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

    /*--------------------------------------------------*
     *  Undo / Redo support                             *
     *--------------------------------------------------*/

    /** Immutable snapshot of every node on the board. */
    private static class GameState {
        private final GameNode[][] snapshot;

        private GameState(GameNode[][] original) {
            int r = original.length;
            int c = original[0].length;
            this.snapshot = new GameNode[r][c];
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    this.snapshot[i][j] = original[i][j].copy();
                }
            }
        }
    }

    /** Push current position onto undoStack and clear redoStack. */
    public void commitMove() {
        undoStack.push(createSnapshot());
        redoStack.clear();
    }

    /** Undo the last move; returns {@code true} if something was undone. */
    public boolean undo() {
        if (undoStack.size() <= 1) {
            return false;            // nothing to undo (or only initial state)
        }
        redoStack.push(undoStack.pop());   // current → redo
        restore(undoStack.peek());         // previous ← current
        return true;
    }

    /** Redo the most recently undone move; returns {@code true} if something was redone. */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;            // nothing to redo
        }
        GameState next = redoStack.pop();  // next ← redo
        undoStack.push(next);              // next → undo (current)
        restore(next);
        return true;
    }


    /** Create a deep snapshot of the current board. */
    private GameState createSnapshot() {
        return new GameState(this.nodes);
    }

    /** Replace the board with the nodes stored in {@code state}. */
    private void restore(GameState state) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.nodes[i][j] = state.snapshot[i][j].copy();
            }
        }
        updatePowerPropagation();       // relight bulbs after restoring
    }
}