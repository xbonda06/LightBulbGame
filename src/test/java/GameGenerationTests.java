import common.Position;
import game.Game;
import common.GameNode;
import common.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameGenerationTests {

    @Test
    public void testCreateValidSize() {
        Game game = Game.generate(10, 12);
        assertEquals(10, game.rows(), "Incorrect number of rows.");
        assertEquals(12, game.cols(), "Incorrect number of cols.");
    }

    @Test
    public void testExactlyOnePowerNode() {
        Game game = Game.generate(8, 8);
        int powerCount = 0;
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                if (game.node(new Position(r, c)).isPower()) {
                    powerCount++;
                }
            }
        }
        assertEquals(1, powerCount, "There must be exactly one power node.");
    }

    @Test
    public void testPowerIsAlwaysGenerated() {
        for (int i = 0; i < 50; i++) {
            int size = i/10 + 3;
            Game game = Game.generate(size, size);
            boolean powerFound = false;
            for (int r = 1; r <= game.rows(); r++) {
                for (int c = 1; c <= game.cols(); c++) {
                    if (game.node(new Position(r, c)).isPower()) {
                        powerFound = true;
                        break;
                    }
                }
                if (powerFound) break;
            }
            assertTrue(powerFound, "Power node must be generated in the game.");
        }
    }

    @Test
    public void testAllNodesConnected() {
        Game game = Game.generate(5, 5);

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                assertTrue(game.node(new Position(r, c)).light(),
                        "Node at (" + r + "," + c + ") should be lit.");
            }
        }
    }

    @Test
    public void testAllNodesNonEmpty() {
        Game game = Game.generate( 7, 7);

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                assertTrue(node.isPower() || node.isBulb() || node.isLink(),
                        "Node at (" + r + "," + c + ") should not be empty.");
            }
        }
    }

    @Test
    public void testBulbsAtLeaves() {
        Game game = Game.generate(9, 9);

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                if (node.isBulb()) {
                    assertEquals(1, countConnections(node),
                            "Bulb should have exactly one connection.");
                }
            }
        }
    }

    private int countConnections(GameNode node) {
        int count = 0;
        for (Side side : Side.values()) {
            if (node.containsConnector(side)) {
                count++;
            }
        }
        return count;
    }
}