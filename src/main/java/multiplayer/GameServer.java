package multiplayer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import game.Game;
import json.GameSerializer;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.gson.JsonArray;

/**
 * Implements a multiplayer game server using Java Sockets.
 * <p>
 * Handles multiple clients, generates a common game board,
 * and broadcasts messages. Each client is assigned a unique player ID.
 * Game can only be started by the first connected player.
 * </p>
 *
 * @author Andrii Bondarenko (xbonda06)
 */
public class GameServer {
    private final int difficulty;
    private final int port;
    private final int maxPlayers = 4;
    private final GameSerializer gameSerializer = new GameSerializer();
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private final Deque<Integer> availablePlayerIds = new ConcurrentLinkedDeque<>();

    private Game game;
    private String gameJson;
    private final AtomicBoolean gameStarted = new AtomicBoolean(false);


    /**
     * Constructs a GameServer listening on the given port and with the specified difficulty.
     *
     * @param port       The TCP port to listen on.
     * @param difficulty The difficulty level of the generated game.
     */
    public GameServer(int port, int difficulty) {
        this.port = port;
        this.difficulty = difficulty;
        for (int i = 1; i <= maxPlayers; i++) {
            availablePlayerIds.add(i);
        }
    }

    /**
     * Stops the server, closes all client connections, and shuts down the thread pool.
     */
    public void stop() {
        try {
            System.out.println("SERVER: Stopping server...");
            for (ClientHandler client : clients) {
                client.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the current game instance.
     *
     * @return the generated game object
     */
    public Game getGame () {
        return game;
    }

    /**
     * Starts the server. Accepts incoming connections,
     * assigns player IDs, and handles communication between clients.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            System.out.println("SERVER: Server is started on address " + getIpAddress() + ":" + port + ", waiting for players...");

            this.game = Game.generate(difficulty, difficulty);
            game.randomizeRotations();
            gameSerializer.serialize(game, 1);
            gameJson = gameSerializer.getJson();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                if (gameStarted.get()) {
                    System.out.println("SERVER: Game already started. Rejecting new connection.");
                    clientSocket.close();
                    continue;
                }

                Integer assignedId = availablePlayerIds.poll();

                if (assignedId == null) {
                    System.out.println("SERVER: Server is full. Rejecting connection.");
                    clientSocket.close();
                    continue;
                }

                ClientHandler handler = new ClientHandler(clientSocket, assignedId);
                clients.add(handler);
                pool.execute(handler);
                System.out.println("SERVER: Player " + handler.playerId + " connected.");
            }

        } catch (SocketException e) {
            System.out.println("SERVER: Server socket was closed. Server is shutting down.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the public IPv4 address of the server, if available.
     *
     * @return the detected IP address or "127.0.0.1" as fallback
     */
    public String getIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()
                            && !addr.isMulticastAddress() && !addr.isAnyLocalAddress() && !addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1"; // fallback
    }

    /**
     * Sends a message to all clients except the sender.
     *
     * @param message the message to be sent
     * @param sender  the client who sent the original message
     */
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Handles communication with a single client in a separate thread.
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a client handler for a connected socket and assigns a player ID.
         *
         * @param socket   the client's socket
         * @param playerId the unique ID assigned to the player
         */
        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        /**
         * Starts the message listening loop for this client.
         * Handles specific types of messages and communicates with other players.
         */
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(initMessage());
                out.println(playerCountMessage());
                broadcast(playerCountMessage().toString(), this);

                String line;
                while ((line = in.readLine()) != null) {
                    JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                    String type = obj.get("type").getAsString();

                    if ("start_game".equals(type) && playerId == 1) {
                        gameStarted.set(true);
                        JsonObject startMsg = new JsonObject();
                        startMsg.addProperty("type", "start_game");
                        broadcast(startMsg.toString(), null);
                    } else if ("player_count".equals(type)) {
                        JsonObject resp = playerCountMessage();
                        out.println(resp);
                    } else {
                        broadcast(line, this);
                    }
                }
            } catch (IOException e) {
                System.out.println("SERVER: Player " + playerId + " disconnected.");
            } finally {
                try {
                    socket.close();
                    clients.remove(this);
                    if (!gameStarted.get()) {
                        availablePlayerIds.addFirst(playerId);
                        System.out.println("SERVER: Player " + playerId + " ID released.");
                    }
                } catch (IOException ignored) {}
            }
        }

        /**
         * Sends a message to the client.
         *
         * @param message the message to send
         */
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Builds and returns the initial message sent to a connected client.
         *
         * @return JSON string with player ID and initial game state
         */
        private String initMessage() {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "init");
            obj.addProperty("playerId", playerId);
            JsonElement gameJsonElement = JsonParser.parseString(gameJson);
            obj.add("gameJson", gameJsonElement);
            return obj.toString();
        }

        /**
         * Constructs and returns a JSON object containing the current player count and their IDs.
         *
         * @return JSON object with type, count, and player IDs
         */
        private JsonObject playerCountMessage() {
            JsonObject resp = new JsonObject();
            resp.addProperty("type", "player_count_response");

            List<Integer> ids = clients.stream()
                    .map(handler -> handler.playerId)
                    .toList();

            resp.addProperty("count", ids.size());

            JsonArray arr = new JsonArray();
            for (int id : ids) arr.add(id);
            resp.add("playerIds", arr);

            return resp;
        }

        /**
         * Closes the socket connection for the client.
         */
        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
