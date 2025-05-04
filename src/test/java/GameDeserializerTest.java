import common.Position;
import game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class GameSerializerTest {

    private Path logFile;
    private Game game;

    @BeforeEach
    void setup() throws Exception {
        // ensure our logs directory exists
        Path logsDir = Paths.get("logs");
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }

        // point serializer at a fresh test file
        logFile = logsDir.resolve("test.json");
        Files.deleteIfExists(logFile);

        game = Game.create(4, 4);
        game.clearHistory();
    }

    @Test
    void testSerializationOnMove() throws Exception {
        // instead of game.makeMove(new Position(2,3));
        game.node(new Position(2, 3)).turn();

        String logged = Files.readString(logFile);
        assertTrue(logged.contains("\"moveCount\":1"),
                "After one turn(), moveCount should be 1 in the JSON log");
    }

    @Test
    void testMultipleMoves() throws Exception {
        game.node(new Position(1, 1)).turn();
        game.node(new Position(3, 4)).turn();

        String logged = Files.readString(logFile);
        assertTrue(logged.contains("\"moveCount\":2"),
                "After two turn() calls, moveCount should be 2 in the JSON log");
    }

    @Test
    void testUndoDoesNotIncrementMoveCount() throws Exception {
        game.node(new Position(2, 2)).turn();
        assertTrue(game.undo(), "undo() should return true when there is history");

        String logged = Files.readString(logFile);
        // the first turn() produced moveCount=1, undo() does not bump it further
        assertTrue(logged.contains("\"moveCount\":1"),
                "Undo should not bump moveCount");
    }
}