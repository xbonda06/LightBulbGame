package multiplayer;

import common.Position;
import game.Game;
import gui.controllers.GameStartListener;
import gui.controllers.GameUpdateListener;
import json.GameDeserializer;
import com.google.gson.*;

import java.io.*;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GameClient {

    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int playerId;
    private Game ownGame;
    private int latestPlayerCount = -1;
    private List<Integer> latestPlayerIds = new ArrayList<>();
    private final Map<Integer, Game> opponentGames = new HashMap<>();
    private volatile boolean gameStarted = false;

    private GameStartListener startListener;
    private GameUpdateListener gameUpdateListener;

    private final Gson gson = new Gson();

    // debug fields used for testing
    private final List<Position> receivedMoves = new ArrayList<>();
    private final Map<Integer, Stack<Position>> opponentUndoStacks = new HashMap<>();
    private final Map<Integer, Stack<Position>> opponentRedoStacks = new HashMap<>();

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void stop() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("CLIENT: Disconnected from server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void start() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(this::listen).start();
    }

    private void listen() {
        playerId = -1;
        try {
            String line;
            while ((line = in.readLine()) != null) {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                String type = obj.get("type").getAsString();

                switch (type) {
                    case "init" -> {
                        playerId = obj.get("playerId").getAsInt();
                        String gameJson = obj.get("gameJson").toString();
                        ownGame = deserializeGame(gameJson);
                        for (int i = 1; i <= 4; i++) {
                            if (i != playerId) {
                                Game g = deserializeGame(gameJson);
                                opponentGames.put(i, g);
                                opponentUndoStacks.put(i, new Stack<>());
                                opponentRedoStacks.put(i, new Stack<>());
                            }
                        }
                        System.out.println("CLIENT: Connected as player " + playerId);
                    }

                    case "turn" -> {
                        int r = obj.getAsJsonObject("position").get("row").getAsInt();
                        int c = obj.getAsJsonObject("position").get("col").getAsInt();
                        int sender = obj.get("playerId").getAsInt();

                        if (sender != playerId) {
                            Position pos = new Position(r, c);
                            receivedMoves.add(pos);
                            Game g = opponentGames.get(sender);
                            if (g != null) {
                                g.node(pos).turn();
                                g.setLastTurnedNode(pos);
                                g.updatePowerPropagation();

                                opponentUndoStacks.get(sender).push(pos);
                                opponentRedoStacks.get(sender).clear();
                            }

                            if (gameUpdateListener != null) {
                                gameUpdateListener.onGameUpdate();
                            }
                        }
                    }

                    case "undo" -> {
                        int sender = obj.get("playerId").getAsInt();
                        if (sender != playerId) {
                            Stack<Position> undoStack = opponentUndoStacks.get(sender);
                            Stack<Position> redoStack = opponentRedoStacks.get(sender);
                            if (undoStack != null && !undoStack.isEmpty()) {
                                Position pos = undoStack.pop();
                                Game g = opponentGames.get(sender);
                                if (g != null) {
                                    g.node(pos).turnBack();
                                    g.setLastTurnedNode(pos);
                                    g.updatePowerPropagation();
                                }
                                redoStack.push(pos);
                            }

                            if (gameUpdateListener != null) {
                                gameUpdateListener.onGameUpdate();
                            }
                        }
                    }

                    case "redo" -> {
                        int sender = obj.get("playerId").getAsInt();
                        if (sender != playerId) {
                            Stack<Position> redoStack = opponentRedoStacks.get(sender);
                            Stack<Position> undoStack = opponentUndoStacks.get(sender);
                            if (redoStack != null && !redoStack.isEmpty()) {
                                Position pos = redoStack.pop();
                                Game g = opponentGames.get(sender);
                                if (g != null) {
                                    g.node(pos).turn();
                                    g.setLastTurnedNode(pos);
                                    g.updatePowerPropagation();
                                }
                                undoStack.push(pos);
                            }
                        }

                        if (gameUpdateListener != null) {
                            gameUpdateListener.onGameUpdate();
                        }
                    }

                    case "start_game" -> {
                        gameStarted = true;
                        System.out.println("CLIENT: Game started!");
                        if (startListener != null) {
                            startListener.onGameStarted();
                        }
                    }

                    case "player_count_response" -> {
                        latestPlayerCount = obj.get("count").getAsInt();
                        latestPlayerIds.clear();
                        for (JsonElement el : obj.getAsJsonArray("playerIds")) {
                            latestPlayerIds.add(el.getAsInt());
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
            Path temp = Files.createTempFile("temp_game_", ".json");
            Files.writeString(temp, gameJson);
            GameDeserializer deserializer = new GameDeserializer(temp);
            return deserializer.getGame();
        } catch (Exception e) {
            throw new RuntimeException("Deserialization Error", e);
        }
    }

    public void sendStartGame() {
        if (playerId == 1) { // Only player 1 can start the game
            JsonObject msg = new JsonObject();
            msg.addProperty("type", "start_game");
            out.println(msg);
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

    public void sendUndo() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "undo");
        msg.addProperty("playerId", playerId);
        out.println(msg);
    }

    public void sendRedo() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "redo");
        msg.addProperty("playerId", playerId);
        out.println(msg);
    }

    public void requestPlayerCount() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "player_count");
        out.println(msg);
        System.out.println("CLIENT: Player count requested.");
    }

    public boolean isGameStarted() { return gameStarted; }

    public void setGameStartListener(GameStartListener listener) { this.startListener = listener; }
    public void setGameUpdateListener(GameUpdateListener gameUpdateListener) { this.gameUpdateListener = gameUpdateListener; }

    public int getLatestPlayerCount() { return latestPlayerCount; }
    public List<Integer> getLatestPlayerIds() { return new ArrayList<>(latestPlayerIds); }
    public Game getOwnGame() { return ownGame; }
    public Game getOpponentGame(int id) { return opponentGames.get(id); }
    public Set<Integer> getOpponentIds() { return opponentGames.keySet(); }
    public int getPlayerId() {
        return playerId;
    }
    public List<Position> getReceivedMoves() { return receivedMoves; }
}
