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

public class GameServer {
    private final int difficulty; // standard easy
    private final int port;
    private final int maxPlayers = 4;
    private final GameSerializer gameSerializer = new GameSerializer();
    private final List<ClientHandler> clients = new ArrayList<>();
    private final ExecutorService pool = Executors.newFixedThreadPool(maxPlayers);
    private ServerSocket serverSocket;

    private Game game;
    private String gameJson;

    public GameServer(int port, int difficulty) {
        this.port = port;
        this.difficulty = difficulty;
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
            System.out.println("SERVER: Server is started on address " + InetAddress.getLocalHost().getHostAddress() + ":" + port + ", waiting for other players...");

            this.game = Game.generate(difficulty, difficulty);
            game.randomizeRotations();
            gameSerializer.serialize(game, 1);
            gameJson = gameSerializer.getJson();

            int playerId = 1;

            while (clients.size() < maxPlayers) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, playerId++);
                clients.add(handler);
                pool.execute(handler);
                System.out.println("SERVER: Player " + handler.playerId + " connected.");
            }

        }catch (SocketException e) {
            System.out.println("SERVER: Server socket was closed. Server is shutting down.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
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
                    System.out.println("SERVER: Received from player " + playerId + ": " + line);
                    broadcast(line, this);
                }

            } catch (IOException e) {
                System.out.println("SERVER: Player " + playerId + " disconnected.");
            } finally {
                try {
                    socket.close();
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
