package multiplayer;

import common.Position;
import game.Game;
import gui.controllers.GamePlayerCountListener;
import gui.controllers.GameStartListener;
import gui.controllers.GameUpdateListener;
import gui.controllers.GameWinListener;
import json.GameDeserializer;
import com.google.gson.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a multiplayer game client that connects to a game server.
 * <p>
 * Handles game communication, deserialization of game data, and propagates game events
 * to registered listeners. Each client manages its own game state and observes opponents' moves.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
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
    private GameWinListener gameWinListener;
    private GamePlayerCountListener playerCountListener;

    private final Gson gson = new Gson();

    // debug fields used for testing
    private final List<Position> receivedMoves = new ArrayList<>();
    private final Map<Integer, Stack<Position>> opponentUndoStacks = new HashMap<>();
    private final Map<Integer, Stack<Position>> opponentRedoStacks = new HashMap<>();

    /**
     * Constructs a new GameClient instance.
     *
     * @param host the host address of the server
     * @param port the port number of the server
     */
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Disconnects the client from the server.
     */
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

    /**
     * Establishes a connection to the server and starts listening in a new thread.
     *
     * @throws IOException if the connection cannot be established
     */
    public void start() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(this::listen).start();
    }

    /**
     * Listens to server messages and handles game synchronization and events.
     */
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

                        if(playerCountListener != null) {
                            playerCountListener.onPlayerCountChanged(latestPlayerCount);
                        }
                    }

                    case "win" -> {
                        int winnerId = obj.get("winnerId").getAsInt();
                        if(winnerId == playerId) {
                            System.out.println("CLIENT: You win!");
                        } else {
                            System.out.println("CLIENT: Player " + winnerId + " wins!");
                        }
                        if (gameWinListener != null) {
                            gameWinListener.onGameWin(winnerId);
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

    /**
     * Sends a request to the server to start the game (only player 1 is allowed).
     */
    public void sendStartGame() {
        if (playerId == 1) { // Only player 1 can start the game
            JsonObject msg = new JsonObject();
            msg.addProperty("type", "start_game");
            out.println(msg);
        }
    }

    /**
     * Sends a move (turn) to the server.
     *
     * @param pos the position to be turned
     */
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

    /**
     * Sends an undo command to the server.
     */
    public void sendUndo() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "undo");
        msg.addProperty("playerId", playerId);
        out.println(msg);
    }

    /**
     * Sends an undo command to the server.
     */
    public void sendRedo() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "redo");
        msg.addProperty("playerId", playerId);
        out.println(msg);
    }

    /**
     * Sends a win notification to the server.
     */
    public void sendWin() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "win");
        msg.addProperty("winnerId", playerId);
        out.println(msg);
        if(gameWinListener != null) {
            gameWinListener.onGameWin(playerId);
        }
    }

    /**
     * Requests the current player count from the server.
     */
    public void requestPlayerCount() {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", "player_count");
        out.println(msg);
        System.out.println("CLIENT: Player count requested.");
    }

    /**
     * Checks if the game has started.
     *
     * @return true if the game has started, false otherwise
     */
    public boolean isGameStarted() { return gameStarted; }

    /**
     * Sets the listener that will be triggered when the game starts.
     *
     * @param listener the listener to register
     */
    public void setGameStartListener(GameStartListener listener) { this.startListener = listener; }

    /**
     * Sets the listener that will be triggered when the game updates.
     *
     * @param gameUpdateListener the listener to register
     */
    public void setGameUpdateListener(GameUpdateListener gameUpdateListener) { this.gameUpdateListener = gameUpdateListener; }

    /**
     * Sets the listener that will be triggered when a player wins.
     *
     * @param gameWinListener the listener to register
     */
    public void setGameWinListener(GameWinListener gameWinListener) { this.gameWinListener = gameWinListener;}
    public void setPlayerCountListener(GamePlayerCountListener playerCountListener) { this.playerCountListener = playerCountListener; }

    /**
     * Returns the most recently received player count.
     *
     * @return the latest player count
     */
    public int getLatestPlayerCount() { return latestPlayerCount; }

    /**
     * Returns the list of player IDs currently connected to the server.
     *
     * @return a list of player IDs
     */
    public List<Integer> getLatestPlayerIds() { return new ArrayList<>(latestPlayerIds); }

    /**
     * Returns the current game instance of the client.
     *
     * @return the client's game instance
     */
    public Game getOwnGame() { return ownGame; }

    /**
     * Returns the game instance of an opponent.
     *
     * @param id the ID of the opponent
     * @return the opponent's game or null if not available
     */
    public Game getOpponentGame(int id) { return opponentGames.get(id); }

    /**
     * Returns the set of all opponent player IDs.
     *
     * @return a set of opponent IDs
     */
    public Set<Integer> getOpponentIds() { return opponentGames.keySet(); }

    /**
     * Returns the ID of the current player.
     *
     * @return the player's ID
     */
    public int getPlayerId() { return playerId; }

    /**
     * Returns a list of all received move positions from opponents.
     *
     * @return list of received positions
     */
    public List<Position> getReceivedMoves() { return receivedMoves; }
}
