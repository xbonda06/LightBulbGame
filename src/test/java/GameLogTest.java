import common.Position;
import game.Game;
import common.GameNode;
import common.Side;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

class GameLoggerTest {

    private static final String GAME_ID = "testSave";
    private static final Path LOG_PATH = Paths.get("logs", GAME_ID + ".json");

    @BeforeEach
    void setUp() throws IOException {
        // Ensure logs directory exists and delete any existing log file
        Files.createDirectories(LOG_PATH.getParent());
        Files.deleteIfExists(LOG_PATH);
    }

    @Test
    void loggerShouldWriteSnapshotsAfterEachMove() throws IOException {
        // 1) Create a new game with our test ID
        Game game = Game.create(4, 4);

        // 2) Perform a few moves and commit each one
        game.createLinkNode(new Position(1, 1), Side.NORTH, Side.EAST);

        game.createLinkNode(new Position(2, 2), Side.SOUTH, Side.WEST);

        // 3) Verify the log file was created
        assertTrue(Files.exists(LOG_PATH), "Log file should exist: " + LOG_PATH);

        // 4) Read all lines from the log file
        List<String> lines = Files.readAllLines(LOG_PATH);

        // 5) Print contents for manual inspection (optional)
        System.out.println("=== Contents of " + LOG_PATH + " ===");
        lines.forEach(System.out::println);
        System.out.println("=== End of log ===");

        // 6) Assert that we have at least two JSON entries
        assertFalse(lines.isEmpty(), "Log file should not be empty");
        assertTrue(lines.size() >= 2, "There should be at least 2 JSON snapshots");
    }
}