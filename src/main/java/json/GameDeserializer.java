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
     * Initializes the game board, nodes, and history, and sets the initial step.
     * @param jsonFile path to the saved game file (e.g., "3.json")
     * @throws Exception if reading or parsing fails
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

    public Game getGame() {
        return game;
    }

    public List<Position> getFullHistory() {
        return List.copyOf(fullHistory);
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getTotalSteps() {
        return fullHistory.size();
    }

    /**
     * Moves the game state to a specific step in history,
     * resetting and replaying previous moves.
     * @param step the step number to reach (0 to total steps)
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
     * Moves forward by one step if possible.
     * @return true if successful, false if at end
     */
    public boolean nextStep() {
        if (currentStep < fullHistory.size()) {
            goToStep(currentStep + 1);
            return true;
        }
        return false;
    }

    /**
     * Moves back by one step if possible.
     * @return true if successful, false if at beginning
     */
    public boolean previousStep() {
        if (currentStep > 0) {
            goToStep(currentStep - 1);
            return true;
        }
        return false;
    }

    /**
     * The full snapshot of a game, including
     * board size, node layout, and move history.
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
     * One saved node on the board.
     */
    private static class NodeDto {
        int row, col;
        boolean isPower, isBulb, isLink, isLit;
        List<Side> connectors;
    }

}