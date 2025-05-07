package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.Position;
import common.GameNode;
import game.Game;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameSerializer {
    private static final Path DATA_DIRECTORY = Paths.get("data");

    private Path logFile;
    private final Gson gson;

    private List<NodeDto> initialNodes;
    private boolean initialCaptured = false;


    /**
     * Creates a new serializer, prepares the data directory,
     * and allocates a unique save file name (e.g., 1.json, 2.json, ...).
     */
    public GameSerializer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            Files.createDirectories(DATA_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory", e);
        }
        int id = allocateNextId();
        this.logFile = DATA_DIRECTORY.resolve(id + ".json");
    }

    /**
     * Finds the next available numeric ID for a new save file.
     * Skips missing numbers (e.g., [1, 2, 4] → next ID is 3).
     * @return the next unused game ID
     */
    private int allocateNextId() {
        try {
            List<Integer> ids = Files.list(DATA_DIRECTORY)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5))
                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::valueOf)
                    .sorted()
                    .toList();

            int next = 1;
            for (int id : ids) {
                if (id == next) {
                    next++;
                } else if (id > next) {
                    break;
                }
            }
            return next;
        } catch (IOException e) {
            return 1;
        }
    }

    /**
     * Saves the current game state to the assigned JSON file.
     * Captures the board layout, move count, and undo/redo history.
     * @param game       the game instance to serialize
     * @param moveCount  the number of moves performed
     */
    public void serialize(Game game, int moveCount) {
        if (!initialCaptured) {
            initialNodes = captureNodes(game);
            initialCaptured = true;
        }
        List<Position> undoHistory = extractStack(game, "undoStack");
        List<Position> redoHistory = extractStack(game, "redoStack");

        SnapshotWithHistory dto = new SnapshotWithHistory(
                moveCount,
                Instant.now().toEpochMilli(),
                game.rows(), game.cols(),
                initialNodes,
                undoHistory,
                redoHistory
        );

        String json = gson.toJson(dto);
        try (FileWriter w = new FileWriter(logFile.toFile(), false)) {
            w.write(json);
            w.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Captures the layout and type of all nodes on the board.
     * @param game the current game instance
     * @return a list of serializable node descriptors
     */
    private List<NodeDto> captureNodes(Game game) {
        List<NodeDto> list = new ArrayList<>();
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode n = game.node(new Position(r, c));
                list.add(new NodeDto(r, c, n.isPower(), n.isBulb(), n.isLink(), n.getConnectors()));
            }
        }
        return list;
    }


    /**
     * Uses reflection to extract the undo/redo stacks from the game object.
     * @param game      the game instance
     * @param fieldName the name of the stack field ("undoStack" or "redoStack")
     * @return a copy of the stack as a list
     */
    @SuppressWarnings("unchecked")
    private List<Position> extractStack(Game game, String fieldName) {
        try {
            Field f = Game.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            Stack<Position> st = (Stack<Position>) f.get(game);
            return new ArrayList<>(st);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializable data class representing one node on the board.
     */
    private static class NodeDto {
        int row, col;
        boolean isPower, isBulb, isLink;
        List<common.Side> connectors;
        NodeDto(int row, int col, boolean p, boolean b, boolean l, List<common.Side> cn) {
            this.row = row; this.col = col;
            this.isPower = p; this.isBulb = b; this.isLink = l;
            this.connectors = cn;
        }
    }

    private static class SnapshotWithHistory {
        int moveNumber;
        long timestamp;
        int rows, cols;
        List<NodeDto> initialNodes;
        List<Position> undoHistory, redoHistory;
        SnapshotWithHistory(int mn, long ts, int r, int c,
                            List<NodeDto> init,
                            List<Position> undo,
                            List<Position> redo) {
            this.moveNumber   = mn;
            this.timestamp    = ts;
            this.rows         = r;
            this.cols         = c;
            this.initialNodes = init;
            this.undoHistory  = undo;
            this.redoHistory  = redo;
        }

    }

    /**
     * Updates the save file path to a fixed ID (used when reloading existing games).
     * @param id the fixed game ID (e.g., 3 → "3.json")
     */
    public void setFixedFile(int id) {
        this.logFile = DATA_DIRECTORY.resolve(id + ".json");
    }
}