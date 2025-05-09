/**
 * This class is responsible for serializing the state of a game to a JSON file.
 * <p>
 * The serialized output is written to the {@code data/} directory, and formatted
 * using the GSON library for readability.
 * </p>
 *
 * @author Alina Paliienko (xpaliia00)
 */
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

    private String json;

    private List<NodeDto> initialNodes;
    private boolean initialCaptured = false;


    /**
     * Constructs a new {@code GameSerializer}, ensures the data directory exists,
     * and allocates a unique save file name (e.g., {@code 1.json}, {@code 2.json}, ...).
     *
     * @throws RuntimeException if the data directory cannot be created
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
     * Existing saved game files in the {@code data/} directory are scanned,
     * and the smallest missing positive integer is returned
     * (e.g., for files {@code [1.json, 2.json, 4.json]}, the next ID is {@code 3}).
     *
     * @return the next unused game ID, or {@code 1} if the directory cannot be read
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
     * The serialized data includes the initial board layout (captured only once),
     * the current move count, and the undo/redo history stacks.
     *
     * @param game       the {@code Game} instance to serialize
     * @param moveCount  the number of moves performed so far
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

        json = gson.toJson(dto);
        try (FileWriter w = new FileWriter(logFile.toFile(), false)) {
            w.write(json);
            w.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getJson() {
        return json;
    }

    /**
     * Captures the layout and properties of all nodes on the game board.
     * Iterates over the board by rows and columns and collects a list of
     * {@code NodeDto} objects representing each node's position and state,
     * including whether it's a power source, bulb, link, and its connectors.
     *
     * @param game the current {@code Game} instance to inspect
     * @return a list of {@code NodeDto} objects describing each node on the board
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
     * Uses reflection to extract a stack field (e.g., undo or redo history) from the {@code Game} object.
     * The specified field must be a {@code Stack<Position>} declared in the {@code Game} class.
     * This method is typically used for accessing private fields like {@code "undoStack"} or {@code "redoStack"}.
     *
     * @param game      the {@code Game} instance from which to extract the stack
     * @param fieldName the name of the field to extract ({@code "undoStack"} or {@code "redoStack"})
     * @return a copy of the extracted stack as a {@code List<Position>}
     * @throws RuntimeException if the field does not exist or cannot be accessed
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

    /**
     * Represents a snapshot of the game state for serialization,
     * including metadata such as move count, board dimensions,
     * initial node layout, and undo/redo history.
     */
    private static class SnapshotWithHistory {
        int moveNumber;
        long timestamp;
        int rows, cols;
        List<NodeDto> initialNodes;
        List<Position> undoHistory, redoHistory;
        /**
         * Constructs a new snapshot with the specified game data.
         *
         * @param mn    the move number
         * @param ts    the timestamp (in milliseconds since epoch)
         * @param r     number of rows in the game board
         * @param c     number of columns in the game board
         * @param init  list of initial nodes describing the board state
         * @param undo  undo history stack as a list of positions
         * @param redo  redo history stack as a list of positions
         */
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
     *
     * @param id the fixed game ID (e.g., 3 → "3.json")
     */
    public void setFixedFile(int id) {
        this.logFile = DATA_DIRECTORY.resolve(id + ".json");
    }
}