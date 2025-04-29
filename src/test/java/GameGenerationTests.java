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
        // Check that power is generated in the game
        for (int i = 0; i < 50; i++) {
            Game game = Game.generate(i/10 + 1, i/10 + 1);
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
        Game game = Game.generate(5, 5); // Generate an easy mode game

        // After generation, all cells should be powered
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                assertTrue(game.node(new Position(r, c)).light(), "Node at (" + r + "," + c + ") should be lit.");
            }
        }
    }

    @Test
    public void testAllNodesNonEmpty() {
        Game game = Game.generate(7, 7); // Generate a medium mode game

        // Check that each cell is either power, bulb, or link
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                assertTrue(node.isPower() || node.isBulb() || node.isLink(), "Node at (" + r + "," + c + ") should not be empty.");
            }
        }
    }

    @Test
    public void testBulbsAtLeaves() {
        Game game = Game.generate(9, 9); // Generate a hard mode game

        // Bulbs should be placed in nodes with one connection
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                int connections = countConnections(node);
                if (node.isBulb()) {
                    assertEquals(1, connections, "Bulb should have exactly one connection.");
                }
            }
        }
    }

    // Help method to count the number of connectors in a node
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
