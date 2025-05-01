package game;

import common.GameNode;
import common.Position;
import common.Side;
import ija.ija2024.tool.common.Observable;
import ija.ija2024.tool.common.ToolEnvironment;
import ija.ija2024.tool.common.ToolField;

import logging.GameLogger;
import java.nio.file.Paths;

import java.util.*;

public class Game implements ToolEnvironment, Observable.Observer {
    private static long nextId = 1;
    private final long gameId;
    private final int rows;
    private final int cols;

    private final GameLogger logger;

    private final GameNode[][] nodes;
    private boolean isPower = false;

    private int moveCount = 0;
    private final Stack<Position> undoStack = new Stack<>();
    private final Stack<Position> redoStack = new Stack<>();


    private Game(int rows, int cols) {
        this.gameId = nextId++;
        this.rows = rows;
        this.cols = cols;
        this.nodes = new GameNode[rows][cols];
        this.logger = new GameLogger(Paths.get("logs", gameId + ".json"));
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

    public int rows() {
        return this.rows;
    }
    public int cols() {
        return this.cols;
    }
    public GameNode node(Position p) {
        return this.nodes[p.getRow() - 1][p.getCol() - 1];
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

    public void randomizeRotations() {
        Random random = new Random();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Position pos = new Position(r, c);
                GameNode node = node(pos);
                int turns = random.nextInt(4);
                for (int i = 0; i < turns; i++) {
                    node.turn();
                }
                node.setCorrectRotation(turns);
                node.resetCurrentRotation();
            }
        }
    }

    // Checks whether all bulbs in the game are lit
    public boolean checkWin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                GameNode node = nodes[r][c];
                if (node.isBulb() && !node.light()) {
                    return false;
                }
            }
        }
        return true;
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

    private void propagateLight(GameNode start) {
        boolean[][] visited = new boolean[rows][cols];
        dfs(start.getPosition(), visited);
    }
    public static Game generate(String gameId, int rows, int cols) {
        if (rows <= 0 || cols <= 0)
            throw new IllegalArgumentException("Invalid game size.");

    private void dfs(Position pos, boolean[][] visited) {
        int r = pos.getRow() - 1;
        int c = pos.getCol() - 1;
        Game game = new Game(gameId, rows, cols);
        Random random = new Random();

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
    public void makeMove(Position p) {
        // actually rotate the node
        node(p).turn();
        updatePowerPropagation();

        // record the move
        undoStack.push(p);
        redoStack.clear();
        moveCount++;
        logger.log(this, moveCount);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Position last = undoStack.pop();

        GameNode n = node(last);
        n.turnBack();
        updatePowerPropagation();

        // record for redo
        redoStack.push(last);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Position next = redoStack.pop();
        // re‐apply the original single 90° turn
        GameNode n = node(next);
        n.turn();
        updatePowerPropagation();
        // push back onto undo
        undoStack.push(next);
        return true;
    }






}