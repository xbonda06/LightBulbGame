package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import common.*;
import game.Game;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameDeserializer {
    private final Path logFile;
    private final Gson gson;

    public GameDeserializer(Path logFile) {
        this.logFile = logFile;
        this.gson = new GsonBuilder().create();
    }

    public Game read() throws IOException {
        try (Reader r = Files.newBufferedReader(logFile)) {
            GameState state = gson.fromJson(r, GameState.class);

            Game game = Game.create(state.rows, state.cols);

            for (NodeState ns : state.nodes) {
                Position p = new Position(ns.row, ns.col);

                if(!(ns.isLink && ns.isBulb && ns.isPower))
                    throw new MalformedJsonException("ERROR: Node cannot be empty");

                Side[] sides = ns.connectors.stream()
                        .map(Side::valueOf)
                        .toArray(Side[]::new);

                if(ns.isPower)
                    game.createPowerNode(p, sides);
                else if(ns.isBulb)
                    game.createBulbNode(p, sides[0]);
                else if(ns.isLink)
                    game.createLinkNode(p, sides);

                game.node(p).setLit(ns.isLit);
            }
            return game;
        }
    }
}