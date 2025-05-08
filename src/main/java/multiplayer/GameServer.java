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

    public GameServer(int port, int difficulty) {
        this.port = port;
        this.difficulty = difficulty;
        for (int i = 1; i <= maxPlayers; i++) {
            availablePlayerIds.add(i);
        }
    }

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

    public Game getGame () {
        return game;
    }

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

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket, int playerId) {
            this.socket = socket;
            this.playerId = playerId;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(initMessage());

                String line;
                while ((line = in.readLine()) != null) {
                    JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                    String type = obj.get("type").getAsString();

                    if ("start_game".equals(type) && playerId == 1) {
                        gameStarted.set(true);
                        JsonObject startMsg = new JsonObject();
                        startMsg.addProperty("type", "start_game");
                        broadcast(startMsg.toString(), null);
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

        public void sendMessage(String message) {
            out.println(message);
        }

        private String initMessage() {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "init");
            obj.addProperty("playerId", playerId);
            JsonElement gameJsonElement = JsonParser.parseString(gameJson);
            obj.add("gameJson", gameJsonElement);
            return obj.toString();
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
