package json;

import common.Position;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameArchiveTest {

    private static final Path DATA_DIR = Paths.get("data");

    @BeforeEach
    void setUp() throws IOException {
        // Clean out and recreate data/ on each run
        if (Files.exists(DATA_DIR)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA_DIR)) {
                for (Path p : ds) Files.delete(p);
            }
        } else {
            Files.createDirectories(DATA_DIR);
        }
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        // Clean up after tests
        if (Files.exists(DATA_DIR)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA_DIR)) {
                for (Path p : ds) Files.delete(p);
            }
            Files.delete(DATA_DIR);
        }
    }

    @Test
    void listSavedGameIds_shouldReturnSortedNumericIdsOnly() throws IOException {
        // create some files in data/
        Files.writeString(DATA_DIR.resolve("3.json"), "{}");
        Files.writeString(DATA_DIR.resolve("1.json"), "{}");
        Files.writeString(DATA_DIR.resolve("2.json"), "{}");
        Files.writeString(DATA_DIR.resolve("foo.json"), "{}");    // non-numeric prefix
        Files.writeString(DATA_DIR.resolve("4.txt"), "ignore");   // wrong extension

        List<Integer> ids = GameArchive.listSavedGameIds();
        assertEquals(List.of(1, 2, 3), ids);
    }

    @Test
    void delete_shouldRemoveTheFile_and_listUpdates() throws IOException {
        Files.writeString(DATA_DIR.resolve("10.json"), "{}");
        Files.writeString(DATA_DIR.resolve("20.json"), "{}");
        assertEquals(List.of(10, 20), GameArchive.listSavedGameIds());

        GameArchive.delete(10);
        assertFalse(Files.exists(DATA_DIR.resolve("10.json")));
        assertEquals(List.of(20), GameArchive.listSavedGameIds());
    }

    @Test
    void load_shouldReturnDeserializer_forValidSave() throws Exception {
        // write a minimal valid snapshot
        String minimal = """
            {
              "moveNumber": 0,
              "timestamp": 0,
              "rows": 1,
              "cols": 1,
              "initialNodes": [],
              "undoHistory": [],
              "redoHistory": []
            }
            """;
        Files.writeString(DATA_DIR.resolve("7.json"), minimal);

        GameDeserializer deser = GameArchive.load(7);
        assertNotNull(deser);

        // the deserialized Game should be 1×1 and have no moves
        var game = deser.getGame();
        assertEquals(1, game.rows());
        assertEquals(1, game.cols());
        assertEquals(0, deser.getTotalSteps());
        assertTrue(deser.getFullHistory().isEmpty());
    }

    @Test
    void load_nonexistent_shouldThrow() {
        var ex = assertThrows(IllegalArgumentException.class, () -> GameArchive.load(999));
        assertTrue(ex.getMessage().contains("No save with id=999"));
    }
}