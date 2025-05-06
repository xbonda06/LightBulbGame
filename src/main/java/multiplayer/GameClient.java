package multiplayer;

import common.Position;
import game.Game;
import json.GameDeserializer;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GameClient {

    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int playerId;
    private Game game;

    private final Gson gson = new Gson();

    private final List<Position> receivedMoves = new ArrayList<>();

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                String type = obj.get("type").getAsString();

                switch (type) {
                    case "init" -> {
                        playerId = obj.get("playerId").getAsInt();
                        String gameJson = obj.get("gameJson").toString();
                        game = deserializeGame(gameJson);
                        System.out.println("CLIENT: Connected as player " + playerId);
                    }

                    case "turn" -> {
                        int r = obj.getAsJsonObject("position").get("row").getAsInt();
                        int c = obj.getAsJsonObject("position").get("col").getAsInt();
                        int sender = obj.get("playerId").getAsInt();

                        if (sender != playerId) {
                            Position pos = new Position(r, c);
                            receivedMoves.add(pos);
                            game.node(pos).turn();
                            game.setLastTurnedNode(pos);
                            game.updatePowerPropagation();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("CLIENT " + playerId + ": Connection lost.");
        }
    }

    private Game deserializeGame(String gameJson) {
        try {
            Path temp = Files.createTempFile("game", ".json");
            Files.writeString(temp, gameJson);
            GameDeserializer deserializer = new GameDeserializer(temp);
            return deserializer.getGame();
        } catch (Exception e) {
            throw new RuntimeException("Deserialization Error", e);
        }
    }

    public void sendTurn(Position pos) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "turn");
        msg.addProperty("playerId", playerId);

        JsonObject posJson = new JsonObject();
        posJson.addProperty("row", pos.getRow());
        posJson.addProperty("col", pos.getCol());
        msg.add("position", posJson);

        out.println(msg);
    }

    public Game getGame() {
        return game;
    }

    public int getPlayerId() {
        return playerId;
    }

    public List<Position> getReceivedMoves() {
        return receivedMoves;
    }
}
