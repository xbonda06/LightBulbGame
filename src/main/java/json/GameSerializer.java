package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import common.Position;
import game.Game;
import common.GameNode;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameSerializer {
    private static final Path DATA_DIRECTORY = Paths.get("data");

    private final Path logFile;
    private final Gson gson;

    // will hold the snapshot of nodes right after randomizeRotations()
    private List<NodeDto> initialNodes;
    private boolean initialCaptured = false;

    public GameSerializer(Path originalLogFile) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try {
            Files.createDirectories(DATA_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directory", e);
        }

        this.logFile = DATA_DIRECTORY.resolve(originalLogFile.getFileName());
    }

    public void serialize(Game game, int moveCount) {
        if (moveCount == 0) {
            return;
        }

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
        try (FileWriter writer = new FileWriter(logFile.toFile(), false)) {
            writer.write(json);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<NodeDto> captureNodes(Game game) {
        List<NodeDto> list = new ArrayList<>();
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                list.add(new NodeDto(
                        r, c,
                        node.isPower(),
                        node.isBulb(),
                        node.isLink(),
                        node.getConnectors()
                ));
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<Position> extractStack(Game game, String fieldName) {
        try {
            Field f = Game.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            Stack<Position> stack = (Stack<Position>) f.get(game);
            // return a shallow copy
            return new ArrayList<>(stack);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract stack " + fieldName, e);
        }
    }

    /** DTO that represents one node for JSON. */
    private static class NodeDto {
        int row, col;
        boolean isPower, isBulb, isLink;
        List<common.Side> connectors;

        NodeDto(int row, int col,
                boolean isPower, boolean isBulb, boolean isLink,
                List<common.Side> connectors) {
            this.row = row;
            this.col = col;
            this.isPower = isPower;
            this.isBulb = isBulb;
            this.isLink = isLink;
            this.connectors = connectors;
        }
    }

    /** JSON payload for a snapshot with history. */
    private static class SnapshotWithHistory {
        int moveNumber;
        long timestamp;
        int rows, cols;
        List<NodeDto> initialNodes;
        List<Position> undoHistory;
        List<Position> redoHistory;

        SnapshotWithHistory(int moveNumber,
                            long timestamp,
                            int rows, int cols,
                            List<NodeDto> initialNodes,
                            List<Position> undoHistory,
                            List<Position> redoHistory) {
            this.moveNumber    = moveNumber;
            this.timestamp     = timestamp;
            this.rows          = rows;
            this.cols          = cols;
            this.initialNodes  = initialNodes;
            this.undoHistory   = undoHistory;
            this.redoHistory   = redoHistory;
        }
    }
}