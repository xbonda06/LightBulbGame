package log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.lang.ref.Cleaner;

/**
 * Utility class for logging the progress of a game to a timestamped file.
 * <p>
 * Automatically creates a log file in the {@code logs/} directory.
 * Logging is finalized and the writer is closed automatically via {@link java.lang.ref.Cleaner}
 * when the object becomes unreachable or is garbage collected.
 * </p>
 *
 * <p>
 * Each game session is logged in a separate file, and all log entries are timestamped.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
public class GameLogger {

    private static final Cleaner cleaner = Cleaner.create();

    private final BufferedWriter writer;
    private final Cleaner.Cleanable cleanable;

    /**
     * Inner class that defines cleanup behavior when the GameLogger is no longer referenced.
     * Automatically writes a closing message and closes the writer.
     */
    private static class LoggerState implements Runnable {
        private final BufferedWriter writer;

        /**
         * Constructs a cleanup state with the given writer.
         *
         * @param writer the writer to close during cleanup
         */
        LoggerState(BufferedWriter writer) {
            this.writer = writer;
        }

        /**
         * Called when the GameLogger is cleaned up by the Cleaner.
         * Writes the end-of-game message and closes the log file.
         */
        @Override
        public void run() {
            try {
                writer.write("=== Game ended ===\n");
                writer.close();
            } catch (IOException e) {
                System.err.println("LOGGER CLEANUP ERROR: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new logger for a game session.
     * A log file is created in the {@code logs/} directory with the game ID and timestamp.
     *
     * @param gameId the ID of the game session being logged
     * @throws RuntimeException if the log file cannot be created
     */
    public GameLogger(long gameId) {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path logFile = logDir.resolve("game_" + gameId + "_" + timestamp + ".log");

            this.writer = new BufferedWriter(new FileWriter(logFile.toFile(), true));
            log("=== Game " + gameId + " started at " + timestamp + " ===");

            LoggerState state = new LoggerState(writer);
            this.cleanable = cleaner.register(this, state);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize game logger", e);
        }
    }

    /**
     * Writes a message to the log file with a timestamp.
     *
     * @param message the message to be logged
     */
    public void log(String message) {
        try {
            writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " - " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("LOGGER ERROR: " + e.getMessage());
        }
    }
}
