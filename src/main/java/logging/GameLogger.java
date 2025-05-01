package logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class GameLogger {
    private final Path logFile;
    private final Gson gson;

    public GameLogger(Path logFile) {
        this.logFile = logFile;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(game.Game game, int moveCount) {
        GameSnapshotDto snapshot = new GameSnapshotDto(game, moveCount, Instant.now());
        String json = gson.toJson(snapshot);

        try (FileWriter fw = new FileWriter(logFile.toFile(), false)) {
            fw.write(json);
            fw.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
