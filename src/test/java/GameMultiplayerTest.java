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
}
