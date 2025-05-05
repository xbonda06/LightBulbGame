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
    private final Gson gson = new GsonBuilder().create();
    private final SnapshotWithHistory snapshot;
    private final Game game;
    private final List<Position> fullHistory;
    private int currentStep = 0;

    public GameDeserializer(Path jsonFile) throws Exception {
        try (Reader reader = Files.newBufferedReader(jsonFile)) {
            snapshot = gson.fromJson(reader, SnapshotWithHistory.class);
        }

        game = Game.create(snapshot.rows, snapshot.cols);
        game.clearHistory();

        for (NodeDto n : snapshot.initialNodes) {
            Position p = new Position(n.row, n.col);
            GameNode node = game.node(p);
            if (n.isPower) {
                node.setPower(n.connectors.toArray(new Side[0]));
            } else if (n.isBulb) {
                node.setBulb(n.connectors.get(0));
            } else if (n.isLink) {
                node.setLink(n.connectors.toArray(new Side[0]));
            }
        }
        game.init();

        fullHistory = new ArrayList<>(snapshot.undoHistory);
        for (int i = snapshot.redoHistory.size() - 1; i >= 0; i--) {
            fullHistory.add(snapshot.redoHistory.get(i));
        }

        goToStep(0);
        game.loadHistory(snapshot.undoHistory, snapshot.redoHistory);
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
            // record as "last turned" so getLastTurnedNode() works
            game.setLastTurnedNode(move);
        }
        currentStep = step;
    }

    public boolean nextStep() {
        if (currentStep < fullHistory.size()) {
            goToStep(currentStep + 1);
            return true;
        }
        return false;
    }

    public boolean previousStep() {
        if (currentStep > 0) {
            goToStep(currentStep - 1);
            return true;
        }
        return false;
    }

    private static class SnapshotWithHistory {
        @SerializedName("moveNumber")    int moveNumber;
        @SerializedName("timestamp")     long timestamp;
        @SerializedName("rows")          int rows;
        @SerializedName("cols")          int cols;
        @SerializedName("initialNodes")  List<NodeDto> initialNodes;
        @SerializedName("undoHistory")   List<Position> undoHistory;
        @SerializedName("redoHistory")   List<Position> redoHistory;
    }

    private static class NodeDto {
        int row, col;
        boolean isPower, isBulb, isLink, isLit;
        List<Side> connectors;
    }
}