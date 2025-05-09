package log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.lang.ref.Cleaner;

public class GameLogger {
    private static final Cleaner cleaner = Cleaner.create();

    private final BufferedWriter writer;
    private final Cleaner.Cleanable cleanable;

    private static class LoggerState implements Runnable {
        private final BufferedWriter writer;

        LoggerState(BufferedWriter writer) {
            this.writer = writer;
        }

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

