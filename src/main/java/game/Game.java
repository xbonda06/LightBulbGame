package game;

import common.GameNode;
import common.Position;
import common.Side;
import log.GameLogger;
import ija.ija2024.tool.common.Observable;
import ija.ija2024.tool.common.ToolEnvironment;
import ija.ija2024.tool.common.ToolField;

import json.GameSerializer;


import java.util.*;

public class Game implements ToolEnvironment, Observable.Observer {
    private static long nextId = 1;
    private final long gameId;
    private final int rows;
    private final int cols;

    private boolean suppressRecording = false;

    private final GameSerializer serializer;

    private final GameNode[][] nodes;
    private boolean isPower = false;

    private int moveCount = 0;
    private Position lastTurnedNode;
    private final Stack<Position> undoStack = new Stack<>();
    private final Stack<Position> redoStack = new Stack<>();

    private final GameLogger logger;

    private Game(int rows, int cols) {
        this.gameId = nextId++;
        this.rows = rows;
        this.cols = cols;
        this.nodes = new GameNode[rows][cols];
        this.serializer = new GameSerializer();
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                this.nodes[r - 1][c - 1] = new GameNode(new Position(r, c));
            }
        }
        this.logger = new GameLogger(gameId);
    }

    public static Game create(int rows, int cols) {

        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Invalid game size.");
        }
        //return new Game(rows, cols);
        Game g = new Game(rows, cols);
        g.clearHistory();
        return g;
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
                case EAST -> new Position(pos.getRow(), pos.getCol() + 1);
                case WEST -> new Position(pos.getRow(), pos.getCol() - 1);
            };

            int nr = neighborPos.getRow() - 1;
            int nc = neighborPos.getCol() - 1;

            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

            GameNode neighbor = nodes[nr][nc];
            Side opposite = switch (side) {
                case NORTH -> Side.SOUTH;
                case SOUTH -> Side.NORTH;
                case EAST -> Side.WEST;
                case WEST -> Side.EAST;
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

    // Is called in GameBoardController when click before turn
    public void setLastTurnedNode(Position p) {
        lastTurnedNode = p;
    }

    public Position getLastTurnedNode() {
        return lastTurnedNode;
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

        if (sides.length == 0 || sides.length > 4) {
            return null;
        }

        if (this.isPower) {
            return null;
        }

        GameNode node = this.node(p);
        node.setPower(sides);
        node.addObserver(this);
        nodes[p.getRow() - 1][p.getCol() - 1] = node;
        this.isPower = true;
        return node;
    }

    public GameNode createLinkNode(Position p, Side... sides) {
        if (p.getRow() < 1 || p.getRow() > this.rows || p.getCol() < 1 || p.getCol() > this.cols) {
            return null;
        }

        if (sides.length < 2 || sides.length > 4) {
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

        final int maxAttempts = 10;
        int attempt = 0;

        while (attempt < maxAttempts) {
            Game game = new Game(rows, cols);
            Random random = new Random();

            // Set power to random position
            Position powerPos = new Position(random.nextInt(rows) + 1, random.nextInt(cols) + 1);
            GameNode power = game.createPowerNode(powerPos, Side.NORTH);
            validatePowerConnections(power, rows + 1, cols + 1);

            for (Side side : Side.values()) {
                Position neighbor = game.neighbor(powerPos, side);
                if (neighbor != null) {
                    game.connectNodes(powerPos, neighbor, side);
                }
            }

            // Generate tree of connections and fill the grid
            game.generateFullConnections(powerPos);

            // Count number of bulbs
            int bulbCount = 0;
            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {
                    if (game.node(new Position(r, c)).isBulb()) {
                        bulbCount++;
                    }
                }
            }

            if ((bulbCount >= 1 && game.cols() <= 4 && game.rows() <= 4) ||
                    (bulbCount >= 3 && game.cols() >= 5 && game.rows() >= 5)) {
                game.init();
                game.clearHistory();
                return game;
            }

            attempt++;
        }

        throw new IllegalStateException("Unable to generate valid game with at least one bulb after " + maxAttempts + " attempts.");
    }

    private static void validatePowerConnections(GameNode node, int rows, int cols) {
        int row = node.getPosition().getRow();
        int col = node.getPosition().getCol();
        if (row == 1 && node.containsConnector(Side.NORTH))
            node.deleteConnector(Side.NORTH);
        else if (row == rows && node.containsConnector(Side.SOUTH))
            node.deleteConnector(Side.SOUTH);
        else if (col == 1 && node.containsConnector(Side.WEST))
            node.deleteConnector(Side.WEST);
        else if (col == cols && node.containsConnector(Side.EAST))
            node.deleteConnector(Side.EAST);
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
        suppressRecording = true;
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
        moveCount = 0;
        clearHistory();
        suppressRecording = false;
        this.serializer.serialize(this, moveCount);
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
        logger.log("Player WON the game!");
        return true;
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

    /**
     * Called when a GameNode changes. Updates light propagation,
     * records the move for undo/redo, and saves the game state.
     */
    @Override
    public void update(Observable observable) {
        updatePowerPropagation();

        if (!suppressRecording && observable instanceof GameNode changed) {
            Position pos = changed.getPosition();
            undoStack.push(pos);
            lastTurnedNode = pos;
            logger.log("TURN at the position: " + pos.getRow() + "," + pos.getCol());
        }

        if (!suppressRecording) {
            redoStack.clear();
            moveCount++;
            serializer.serialize(this, moveCount);
        }
    }

    /**
     * Undoes the last player action.
     */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Position last = undoStack.pop();

        GameNode n = node(last);

        suppressRecording = true;
        n.turnBack();
        suppressRecording = false;

        lastTurnedNode = last;
        redoStack.push(last);
        serializer.serialize(this, moveCount);

        logger.log("UNDO at the position: " + last.getRow() + "," + last.getCol());

        return true;
    }

    /**
     * Special undo used in archive replay mode (reverse order).
     */
    public boolean undoArchive() {
        if (undoStack.isEmpty()) return false;
        Position last = undoStack.getFirst();
        undoStack.removeFirst();

        GameNode n = node(last);

        suppressRecording = true;
        n.turn();
        suppressRecording = false;

        lastTurnedNode = last;
        redoStack.insertElementAt(last, 0);
        serializer.serialize(this, moveCount);

        return true;
    }

    /**
     * Redoes the last undone move. Applies the next step and updates state.
     */
    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Position next = redoStack.pop();

        GameNode n = node(next);

        suppressRecording = true;
        n.turn();
        suppressRecording = false;

        lastTurnedNode = next;
        undoStack.push(next);
        serializer.serialize(this, moveCount);

        logger.log("REDO at the position: " + next.getRow() + "," + next.getCol());

        return true;
    }

    /**
     * Special redo used in archive replay mode (reverse order).
     */
    public boolean redoArchive() {
        if (redoStack.isEmpty()) return false;
        Position next = redoStack.getFirst();
        redoStack.removeFirst();

        GameNode n = node(next);

        suppressRecording = true;
        n.turnBack();
        suppressRecording = false;

        lastTurnedNode = next;
        undoStack.insertElementAt(next, 0);
        serializer.serialize(this, moveCount);

        return true;
    }

    /**
     * Formats a stack of positions as a readable string (e.g., [(1,2),(2,3)]).
     */
    private String formatStack(Stack<Position> stack) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < stack.size(); i++) {
            Position p = stack.get(i);
            sb.append("(")
                    .append(p.getRow()).append(",")
                    .append(p.getCol())
                    .append(")");
            if (i < stack.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Clears the undo/redo history and resets move count.
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        moveCount = 0;
        logger.log("History cleared");
    }

    /**
     * Loads undo and redo history into the game.
     */
    public void loadHistory(List<Position> undoHistory, List<Position> redoHistory) {
        undoStack.clear();
        redoStack.clear();
        undoStack.addAll(undoHistory);
        for (int i = redoHistory.size() - 1; i >= 0; i--) {
            redoStack.push(redoHistory.get(i));
        }
    }

    /**
     * Sets the fixed ID for the save file to ensure consistent serialization.
     */
    public void setSaveFileId(int id) {
        this.serializer.setFixedFile(id);
    }
}