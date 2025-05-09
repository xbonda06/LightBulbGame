/**
 * This class handles the deserialization of saved game data from a JSON file.
 * <p>
 * The deserialization process reconstructs the game object, initializes its state,
 * and replays all previous moves to restore the game as it was when saved.
 * </p>
 *
 * <p>
 * Additional helper methods allow navigation through move history step by step.
 * </p>
 *
 * @author Alina Paliienko (xpaliia00)
 */
package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import common.Position;
import common.Side;
import common.GameNode;
import game.Game;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameDeserializer {
    private final Game game;
    private final List<Position> fullHistory;
    private int currentStep = 0;

    /**
     * Reads and reconstructs a saved game from the given JSON file.
     *
     * @param jsonFile path to the saved game file (e.g., {@code "3.json"})
     * @throws Exception if the file cannot be read or parsed correctly
     */
    public GameDeserializer(Path jsonFile) throws Exception {
        SnapshotWithHistory snapshot;
        try (Reader reader = Files.newBufferedReader(jsonFile)) {
            Gson gson = new GsonBuilder().create();
            snapshot = gson.fromJson(reader, SnapshotWithHistory.class);
        }

        game = Game.create(snapshot.rows, snapshot.cols);
        game.clearHistory();

        // Reconstruct initial board state from saved nodes
        for (NodeDto n : snapshot.initialNodes) {
            Position p = new Position(n.row, n.col);
            GameNode node = game.node(p);
            if (n.isPower) {
                node.setPower(n.connectors.toArray(new Side[0]));
            } else if (n.isBulb) {
                node.setBulb(n.connectors.getFirst());
            } else if (n.isLink) {
                node.setLink(n.connectors.toArray(new Side[0]));
            }
            node.addObserver(game);
        }
        game.init();

        // Merge undo and redo history into a full list of moves
        fullHistory = new ArrayList<>(snapshot.undoHistory);
        for (int i = snapshot.redoHistory.size() - 1; i >= 0; i--) {
            fullHistory.add(snapshot.redoHistory.get(i));
        }

        goToStep(0);
        game.loadHistory(snapshot.undoHistory, snapshot.redoHistory);
        // Assign the original save ID (e.g., 3.json → ID 3)
        String fileName = jsonFile.getFileName().toString();
        int id = -1;
        if(!fileName.startsWith("temp_game_"))
            id = Integer.parseInt(fileName.replace(".json", ""));
        game.setSaveFileId(id);
    }

    /**
     * Returns the reconstructed {@code Game} instance loaded from the JSON file.
     *
     * @return the deserialized {@code Game} object
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns a copy of the full move history (undo + redo) in order of execution.
     *
     * @return an unmodifiable list of all moves performed in the game
     */
    public List<Position> getFullHistory() {
        return List.copyOf(fullHistory);
    }

    /**
     * Returns the index of the current step within the full move history.
     *
     * @return the current step number (0-based index)
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Returns the total number of steps (i.e., size of the full move history).
     *
     * @return the total number of moves in the game history
     */
    public int getTotalSteps() {
        return fullHistory.size();
    }

    /**
     * Moves the game state to a specific step in the move history,
     * resetting the board and replaying all moves up to that point.
     *
     * @param step the step number to reach (0 ≤ step ≤ total steps)
     * @throws IllegalArgumentException if the step is out of bounds
     */
    public void goToStep(int step) {
        if (step < 0 || step > fullHistory.size()) {
            throw new IllegalArgumentException("Step out of range: " + step);
        }
        game.clearHistory();
        game.init();
        for (int i = 0; i < step; i++) {
            Position move = fullHistory.get(i);
            GameNode node = game.node(move);
            node.turn();
            game.updatePowerPropagation();
            game.setLastTurnedNode(move);
        }
        currentStep = step;
    }


    /**
     * Advances the game state by one step in the move history, if possible.
     *
     * @return {@code true} if the step was successful,
     * {@code false} if already at the end
     */
    public boolean nextStep() {
        if (currentStep < fullHistory.size()) {
            goToStep(currentStep + 1);
            return true;
        }
        return false;
    }

    /**
     * Reverts the game state by one step in the move history, if possible.
     *
     * @return {@code true} if the step was successful,
     * {@code false} if already at the beginning
     */
    public boolean previousStep() {
        if (currentStep > 0) {
            goToStep(currentStep - 1);
            return true;
        }
        return false;
    }

    /**
     * Represents the complete snapshot of a saved game.
     * This includes metadata (move count, timestamp, board size),
     * the layout of all initial nodes, and the full undo/redo move history.
     */
    private static class SnapshotWithHistory {
        @SerializedName("moveNumber")    int moveNumber;
        @SerializedName("timestamp")     long timestamp;
        @SerializedName("rows")          int rows;
        @SerializedName("cols")          int cols;
        @SerializedName("initialNodes")  List<NodeDto> initialNodes;
        @SerializedName("undoHistory")   List<Position> undoHistory;
        @SerializedName("redoHistory")   List<Position> redoHistory;
    }

    /**
     * Represents a single node on the board as saved in the JSON file.
     * Stores the node's position, type (power, bulb, or link),
     * whether it is currently lit, and its connector directions.
     */
    private static class NodeDto {
        int row, col;
        boolean isPower, isBulb, isLink, isLit;
        List<Side> connectors;
    }

}