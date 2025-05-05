import multiplayer.GameClient;
import multiplayer.GameServer;
import common.Position;
import game.Game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameMultiplayerTest {

    @Test
    public void testClientToClientMoveSync() throws Exception {
        int port = 8888;
        int difficulty = 7;

        // Start server in a separate thread
        new Thread(() -> {
            GameServer server = new GameServer(port, difficulty);
            server.start();
        }).start();

        // Wait for the server to start
        Thread.sleep(500);

        // Start two clients
        GameClient client1 = new GameClient("localhost", port);
        GameClient client2 = new GameClient("localhost", port);

        client1.start();
        client2.start();

        // Wait for the game to be initialized
        Thread.sleep(1000);

        Game game1 = client1.getGame();
        Game game2 = client2.getGame();

        assertNotNull(game1, "Game should be initialized for client1.");
        assertNotNull(game2, "Game should be initialized for client2.");

        // Make a move with client1
        Position move = new Position(2, 2);
        client1.sendTurn(move);

        // Wait for the move to be processed
        Thread.sleep(1000);

        // Check if the move was applied to client2's game
        Position last = client2.getGame().getLastTurnedNode();
        assertEquals(move, last, "Client2 should have received the move from client1.");
    }

    @Test
    public void testAllClientsGetSameGame() throws Exception {
        int port = 8889;
        int difficulty = 7;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient client1 = new GameClient("localhost", port);
        GameClient client2 = new GameClient("localhost", port);

        client1.start();
        client2.start();

        Thread.sleep(1000);

        Game g1 = client1.getGame();
        Game g2 = client2.getGame();

        assertNotNull(g1);
        assertNotNull(g2);

        for (int r = 1; r <= g1.rows(); r++) {
            for (int c = 1; c <= g1.cols(); c++) {
                assertEquals(g1.node(new Position(r, c)).getConnectors(),
                        g2.node(new Position(r, c)).getConnectors(),
                        "Mismatch at (" + r + "," + c + ")");
            }
        }
    }

    @Test
    public void testMoveDoesNotAffectSenderTwice() throws Exception {
        int port = 8890;
        int difficulty = 7;
        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient client = new GameClient("localhost", port);
        client.start();
        Thread.sleep(1000);

        Game g = client.getGame();
        Position move = new Position(2, 2);
        int before = g.node(move).getConnectors().hashCode();

        client.sendTurn(move);
        Thread.sleep(500);

        int after = g.node(move).getConnectors().hashCode();
        assertEquals(before, after, "Sender's node should not be turned again.");
    }

    @Test
    public void testMultipleMovesSyncCorrectly() throws Exception {
        int port = 8891;
        int difficulty = 7;
        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);
        GameClient c3 = new GameClient("localhost", port);

        c1.start(); c2.start(); c3.start();
        Thread.sleep(1500);

        Position m1 = new Position(2, 2);
        Position m2 = new Position(3, 3);

        c1.sendTurn(m1);
        Thread.sleep(1000);
        c2.sendTurn(m2);
        Thread.sleep(1000);

        for (GameClient c : new GameClient[]{c1, c2, c3}) {
            Position actual = c.getGame().getLastTurnedNode();
            assertEquals(3, actual.getRow(), "Expected row = 3");
            assertEquals(3, actual.getCol(), "Expected col = 3");
        }

    }


}
