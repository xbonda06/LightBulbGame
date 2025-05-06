import common.GameNode;
import common.Side;
import multiplayer.GameClient;
import multiplayer.GameServer;
import common.Position;
import game.Game;

import org.junit.jupiter.api.Test;

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
}
