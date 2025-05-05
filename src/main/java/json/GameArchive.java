package json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameArchive {

    private static final Path DATA_DIR = Paths.get("data");

    /**
     * Returns a sorted list of all saved game IDs (the numeric prefix of each .json file).
     */
    public static List<Integer> listSavedGameIds() {
        try {
            return Files.list(DATA_DIR)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5))
                    .filter(GameArchive::isNumeric)
                    .map(Integer::valueOf)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list saved games", e);
        }
    }

    /**
     * Loads the given game ID from disk into a GameDeserializer.
     * Throws if the file does not exist or fails to parse.
     */
    public static GameDeserializer load(int gameId) {
        Path file = DATA_DIR.resolve(gameId + ".json");
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("No save with id=" + gameId);
        }
        try {
            return new GameDeserializer(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load game " + gameId, e);
        }
    }

    /**
     * Deletes the saved-game file for the given ID.
     */
    public static void delete(int gameId) {
        Path file = DATA_DIR.resolve(gameId + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete save " + gameId, e);
        }
    }

    private static boolean isNumeric(String s) {
        for (char c: s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return !s.isEmpty();
    }
}