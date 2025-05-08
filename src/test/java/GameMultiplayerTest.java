import common.GameNode;
import common.Side;
import json.GameSerializer;
import multiplayer.GameClient;
import multiplayer.GameServer;
import common.Position;
import game.Game;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class GameMultiplayerTest {

    @Test
    public void testClientOwnGameInitialized() throws Exception {
        int port = 8888;
        int difficulty = 7;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient client = new GameClient("localhost", port);
        client.start();
        Thread.sleep(1000);

        assertNotNull(client.getOwnGame(), "Client's own game should be initialized.");
    }

    @Test
    public void testOpponentGamesAreInitialized() throws Exception {
        int port = 8889;
        int difficulty = 6;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);

        c1.start(); c2.start();
        Thread.sleep(1000);

        assertEquals(3, c1.getOpponentIds().size(), "Client1 should track 3 other player slots.");
        assertTrue(c1.getOpponentIds().contains(2), "Client1 should track Client2.");
    }

    @Test
    public void testMoveIsNotReappliedToSender() throws Exception {
        int port = 8890;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient client = new GameClient("localhost", port);
        client.start();
        Thread.sleep(1000);

        Position move = new Position(2, 2);
        int before = client.getOwnGame().node(move).getConnectors().hashCode();

        client.sendTurn(move);
        Thread.sleep(500);

        int after = client.getOwnGame().node(move).getConnectors().hashCode();
        assertEquals(before, after, "Sender should not apply their own move twice.");
    }

    @Test
    public void testOpponentReceivesMove() throws Exception {
        int port = 8891;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient sender = new GameClient("localhost", port);
        GameClient receiver = new GameClient("localhost", port);

        sender.start();
        receiver.start();
        Thread.sleep(1500);

        Position move = new Position(3, 3);
        sender.sendTurn(move);
        Thread.sleep(1000);

        for (int pid : receiver.getOpponentIds()) {
            Game g = receiver.getOpponentGame(pid);
            if (g != null && g.getLastTurnedNode() != null && g.getLastTurnedNode().equals(move)) {
                return; // test passed
            }
        }

        fail("Receiver did not reflect move from sender in any opponent copy.");
    }

    @Test
    public void testMultipleOpponentsSync() throws Exception {
        int port = 8892;
        int difficulty = 5;

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
        Thread.sleep(300);
        c2.sendTurn(m2);
        Thread.sleep(1000);

        // Verify c3 sees both moves in opponent copies
        boolean sawM1 = false, sawM2 = false;
        for (int pid : c3.getOpponentIds()) {
            Game g = c3.getOpponentGame(pid);
            Position last = g.getLastTurnedNode();
            if (last != null) {
                if (last.equals(m1)) sawM1 = true;
                if (last.equals(m2)) sawM2 = true;
            }
        }

        assertTrue(sawM1, "Client 3 should see move m1.");
        assertTrue(sawM2, "Client 3 should see move m2.");
    }

    @Test
    public void testAllClientsGetSameGameStateInitially() throws Exception {
        int port = 8896;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);

        c1.start(); c2.start();
        Thread.sleep(1500);

        Game g1 = c1.getOwnGame();
        Game g2 = c2.getOwnGame();

        assertEquals(g1.rows(), g2.rows(), "Game rows should match.");
        assertEquals(g1.cols(), g2.cols(), "Game cols should match.");

        for (int r = 1; r <= g1.rows(); r++) {
            for (int c = 1; c <= g1.cols(); c++) {
                Position pos = new Position(r, c);
                GameNode n1 = g1.node(pos);
                GameNode n2 = g2.node(pos);

                assertEquals(n1.isPower(), n2.isPower(), "Power node mismatch at " + pos);
                assertEquals(n1.isBulb(), n2.isBulb(), "Bulb node mismatch at " + pos);
                assertEquals(n1.isLink(), n2.isLink(), "Link node mismatch at " + pos);
                assertEquals(n1.getConnectors(), n2.getConnectors(), "Connectors mismatch at " + pos);
            }
        }
    }

    @Test
    public void testUndoRedoBroadcast() throws Exception {
        int port = 8893;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient sender = new GameClient("localhost", port);
        GameClient receiver = new GameClient("localhost", port);

        sender.start();
        receiver.start();
        Thread.sleep(1500);

        // Move
        Position move = new Position(2, 2);
        GameNode senderNode = sender.getOwnGame().node(move);
        int originalHash = senderNode.getConnectors().hashCode();

        sender.sendTurn(move);
        Thread.sleep(1000);

        GameNode receiverNode = receiver.getOpponentGame(sender.getPlayerId()).node(move);
        int turnedHash = receiverNode.getConnectors().hashCode();

        assertNotEquals(originalHash, turnedHash, "Opponent should see turned node.");

        // Undo
        sender.sendUndo();
        Thread.sleep(1000);

        int afterUndoHash = receiverNode.getConnectors().hashCode();
        assertEquals(originalHash, afterUndoHash, "Opponent should see undo applied.");

        // Redo
        sender.sendRedo();
        Thread.sleep(1000);

        int afterRedoHash = receiverNode.getConnectors().hashCode();
        assertEquals(turnedHash, afterRedoHash, "Opponent should see redo reapplied.");
    }

    @Test
    public void testMaxPlayerConnections() throws Exception {
        int port = 8894;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);
        GameClient c3 = new GameClient("localhost", port);
        GameClient c4 = new GameClient("localhost", port);

        c1.start(); c2.start(); c3.start(); c4.start();
        Thread.sleep(2000);

        assertEquals(3, c1.getOpponentIds().size(), "Client1 should see 3 opponents.");
        assertEquals(3, c2.getOpponentIds().size(), "Client2 should see 3 opponents.");
        assertEquals(3, c3.getOpponentIds().size(), "Client3 should see 3 opponents.");
        assertEquals(3, c4.getOpponentIds().size(), "Client4 should see 3 opponents.");
    }

    @Test
    public void testSenderDoesNotApplyUndoRedo() throws Exception {
        int port = 8895;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient client = new GameClient("localhost", port);
        client.start();
        Thread.sleep(1500);

        Position move = new Position(2, 3);
        int before = client.getOwnGame().node(move).getConnectors().hashCode();

        client.sendTurn(move);
        Thread.sleep(500);
        int after = client.getOwnGame().node(move).getConnectors().hashCode();
        assertEquals(before, after, "Sender should not apply their own move.");

        client.sendUndo();
        Thread.sleep(500);
        int afterUndo = client.getOwnGame().node(move).getConnectors().hashCode();
        assertEquals(before, afterUndo, "Sender applies undo locally, not through message.");

        client.sendRedo();
        Thread.sleep(500);
        int afterRedo = client.getOwnGame().node(move).getConnectors().hashCode();
        assertEquals(after, afterRedo, "Sender applies redo locally, not through message.");
    }

    @Test
    public void testGameStartNotification() throws Exception {
        int port = 8900;
        int difficulty = 5;

        AtomicBoolean serverStarted = new AtomicBoolean(false);
        new Thread(() -> {
            new GameServer(port, difficulty).start();
            serverStarted.set(true);
        }).start();
        Thread.sleep(1000);

        GameClient admin = new GameClient("localhost", port);
        GameClient client = new GameClient("localhost", port);
        admin.start();
        client.start();
        Thread.sleep(1500);

        admin.sendStartGame();
        Thread.sleep(1000);

        assertTrue(admin.isGameStarted(), "Admin should receive game start");
        assertTrue(client.isGameStarted(), "Client should receive game start");
    }

    @Test
    public void testPlayerIdReuseBeforeGameStart() throws Exception {
        int port = 8901;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        c1.start();
        Thread.sleep(500);
        int c1Id = c1.getPlayerId();
        c1.stop();
        Thread.sleep(1500);

        GameClient c2 = new GameClient("localhost", port);
        c2.start();
        Thread.sleep(500);

        assertEquals(c1Id, c2.getPlayerId(), "New client should get reused ID");
    }

    @Test
    public void testIdsLockedAfterGameStart() throws Exception {
        int port = 8902;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient admin = new GameClient("localhost", port);
        admin.start();
        Thread.sleep(500);
        admin.sendStartGame();
        Thread.sleep(500);
        admin.stop();

        GameClient newClient = new GameClient("localhost", port);
        newClient.start();
        Thread.sleep(1000);

        assertEquals(-1, newClient.getPlayerId(), "Server should reject connections after game start");
    }

    @Test
    public void testOnlyAdminCanStartGame() throws Exception {
        int port = 8903;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);
        c1.start();
        c2.start();
        Thread.sleep(1500);

        c2.sendStartGame();
        Thread.sleep(1000);

        assertFalse(c1.isGameStarted(), "Game should not start from non-admin player");
        assertFalse(c2.isGameStarted(), "Game should not start from non-admin player");
    }

    @Test
    public void testPlayerReconnectionAfterStart() throws Exception {
        int port = 8904;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient admin = new GameClient("localhost", port);
        admin.start();
        Thread.sleep(500);
        admin.sendStartGame();
        Thread.sleep(500);
        admin.stop();

        GameClient newClient = new GameClient("localhost", port);
        newClient.start();
        Thread.sleep(1000);

        assertEquals(-1, newClient.getPlayerId(), "Should not allow new connections after game start");
    }

    @Test
    public void testMultipleAdminsCannotExist() throws Exception {
        int port = 8905;
        int difficulty = 5;

        new Thread(() -> new GameServer(port, difficulty).start()).start();
        Thread.sleep(500);

        GameClient c1 = new GameClient("localhost", port);
        GameClient c2 = new GameClient("localhost", port);
        c1.start();
        c2.start();
        Thread.sleep(1500);

        assertEquals(1, c1.getPlayerId(), "First client should be admin");
        assertNotEquals(1, c2.getPlayerId(), "Second client should not be admin");
    }
}
