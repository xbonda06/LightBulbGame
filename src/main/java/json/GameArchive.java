package json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides functionality for managing saved games in JSON format.
 * <p>
 * This class supports listing, loading, deleting, and retrieving metadata
 * (such as the saved date) for saved game files stored in the {@code data/} directory.
 * </p>
 *
 * <p>
 * Saved games are expected to be stored as JSON files named by their numeric ID
 * (e.g., {@code 3.json}).
 * </p>
 *
 * @author Alina Paliienko (xpaliia00)
 */
public class GameArchive {

    /**
     * Returns a sorted list of all saved game IDs.
     * The IDs are extracted from filenames in the {@code data/} directory
     * (e.g., {@code "3.json"} → ID {@code 3}).
     *
     * @return a sorted list of saved game IDs as {@code Integer}s
     * @throws UncheckedIOException if the files in the {@code data/} directory cannot be listed
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

    /**
     * Loads the given game ID from disk by reading the corresponding JSON file
     * and returning a {@code GameDeserializer} instance.
     *
     * @param gameId the numeric ID of the saved game (e.g., {@code 3} for {@code "3.json"})
     * @return a {@code GameDeserializer} initialized with the game state from the file
     * @throws IllegalArgumentException if the save file with the given ID does not exist
     * @throws RuntimeException if an error occurs while reading or deserializing the file
     */
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

    /**
     * Deletes the saved-game file associated with the given ID.
     *
     * @param gameId the ID of the game to delete (e.g., {@code 3} for {@code "3.json"})
     * @throws UncheckedIOException if an I/O error occurs while attempting to delete the file
     */
    public static void delete(int gameId) {
        Path file = findDataDirectory().resolve(gameId + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete save " + gameId, e);
        }
    }

    /**
     * Returns the date (without time) when the game with the given ID was saved.
     * The date is extracted from the {@code "timestamp"} field in the JSON file.
     *
     * @param gameId the ID of the saved game (e.g., {@code 3} for {@code "3.json"})
     * @return the {@code LocalDate} representing the date the game was saved
     * @throws RuntimeException if the file cannot be read or the timestamp is missing/invalid
     */
    public static LocalDate getGameDate(int gameId) {
        Path file = findDataDirectory().resolve(gameId + ".json");
        try (FileReader reader = new FileReader(file.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            long timestamp = jsonObject.get("timestamp").getAsLong();
            Instant instant = Instant.ofEpochMilli(timestamp);
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            throw new RuntimeException("Could not read timestamp from game " + gameId, e);
        }
    }

    /**
     * Locates the directory where saved games are stored.
     * Searches for the {@code "data/"} directory; intended fallback to
     * {@code "src/test/resources/data/"} for testing is currently disabled.
     *
     * @return the resolved path to the data directory
     * @throws UncheckedIOException if the data directory does not exist
     */
    private static Path findDataDirectory() {
        Path top = Paths.get("data");
        if (Files.isDirectory(top)) {
            return top;
        }
        //Path testRes = Paths.get("src", "test", "resources", "data");
        //if (Files.isDirectory(testRes)) { return testRes; }
        throw new UncheckedIOException(new IOException("Directory does not exist"));
    }

    /**
     * Checks whether the given string contains only numeric digits (0–9).
     *
     * @param s the string to check
     * @return {@code true} if the string is non-empty and contains only digits,
     *         {@code false} otherwise
     */
    private static boolean isNumeric(String s) {
        if (s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}