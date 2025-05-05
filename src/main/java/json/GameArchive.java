package json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class GameArchive {

    /**
     * Returns a sorted list of all saved game IDs
     * (the numeric prefixes of each .json file) from either
     * the project’s top-level data/ directory or the test resources.
     */
    public static List<Integer> listSavedGameIds() {
        Path dataDir = findDataDirectory();
        try {
            return Files.list(dataDir)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5))
                    .filter(GameArchive::isNumeric)
                    .map(Integer::valueOf)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list saved games in " + dataDir, e);
        }
    }

    /** Loads the given game ID from disk into a GameDeserializer. */
    public static GameDeserializer load(int gameId) {
        Path file = findDataDirectory().resolve(gameId + ".json");
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("No save with id=" + gameId);
        }
        try {
            return new GameDeserializer(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load game " + gameId, e);
        }
    }

    /** Deletes the saved-game file for the given ID. */
    public static void delete(int gameId) {
        Path file = findDataDirectory().resolve(gameId + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete save " + gameId, e);
        }
    }

    private static Path findDataDirectory() {
        Path top = Paths.get("data");
        if (Files.isDirectory(top)) {
            return top;
        }
        Path testRes = Paths.get("src", "test", "resources", "data");
        if (Files.isDirectory(testRes)) {
            return testRes;
        }
        throw new UncheckedIOException(new IOException(
                "Neither data/ nor src/test/resources/data/ exists"
        ));
    }

    private static boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}