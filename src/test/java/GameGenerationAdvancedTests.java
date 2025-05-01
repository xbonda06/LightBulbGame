import common.Position;
import game.Game;
import common.GameNode;
import common.Side;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GameGenerationAdvancedTests {

    @Test
    public void testNoDuplicatePowerNodes() {
        Game game = Game.generate("default", 6, 6);
        game.init();
        game.init();

        int powerCount = 0;
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                if (game.node(new Position(r, c)).isPower()) {
                    powerCount++;
                }
            }
        }
        assertEquals(1, powerCount, "After multiple inits, there should still be exactly one power node.");
    }

    @Test
    public void testEachNodeHasCorrectConnections() {
        Game game = Game.generate("default", 6, 6);

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                Position current = new Position(r, c);
                GameNode node = game.node(current);

                for (Side side : Side.values()) {
                    Position neighborPos = neighbor(current, side, game.rows(), game.cols());
                    if (neighborPos != null) {
                        GameNode neighbor = game.node(neighborPos);
                        if (node.containsConnector(side)) {
                            Side opposite = opposite(side);
                            assertTrue(neighbor.containsConnector(opposite),
                                    "Node at " + current.getRow() + "," + current.getCol() +
                                            " is connected to neighbor at " + neighborPos.getRow() + "," + neighborPos.getCol() +
                                            ", but neighbor is not connected back!");
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testPowerReachableFromEveryNode() {
        Game game = Game.generate("default", 5, 5);

        Position powerPos = findPowerPosition(game);
        assertNotNull(powerPos, "Power node must exist.");

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                Position start = new Position(r, c);
                assertTrue(isReachable(game, start, powerPos),
                        "Node at " + r + "," + c + " should be connected to power node.");
            }
        }
    }

    @Test
    public void testRandomizeRotationsDoesNotChangeConnectionsCount() {
        Game game = Game.generate("default", 5, 5);

        int[][] originalConnections = new int[game.rows()][game.cols()];
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                originalConnections[r-1][c-1] = countConnections(game.node(new Position(r, c)));
            }
        }

        game.randomizeRotations();

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                assertEquals(originalConnections[r-1][c-1],
                        countConnections(game.node(new Position(r, c))),
                        "Number of connections at (" + r + "," + c + ") must not change after rotation.");
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

    private Position neighbor(Position p, Side side, int maxRows, int maxCols) {
        int r = p.getRow(), c = p.getCol();
        return switch (side) {
            case NORTH -> (r > 1) ? new Position(r - 1, c) : null;
            case SOUTH -> (r < maxRows) ? new Position(r + 1, c) : null;
            case EAST -> (c < maxCols) ? new Position(r, c + 1) : null;
            case WEST -> (c > 1) ? new Position(r, c - 1) : null;
        };
    }

    private Side opposite(Side side) {
        return switch (side) {
            case NORTH -> Side.SOUTH;
            case SOUTH -> Side.NORTH;
            case EAST -> Side.WEST;
            case WEST -> Side.EAST;
        };
    }

    private Position findPowerPosition(Game game) {
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                Position p = new Position(r, c);
                if (game.node(p).isPower()) {
                    return p;
                }
            }
        }
        return null;
    }

    private boolean isReachable(Game game, Position from, Position target) {
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            if (current.equals(target)) {
                return true;
            }

            GameNode node = game.node(current);
            for (Side side : Side.values()) {
                Position neighbor = neighbor(current, side, game.rows(), game.cols());
                if (neighbor != null && !visited.contains(neighbor)) {
                    GameNode neighborNode = game.node(neighbor);
                    if (node.containsConnector(side) && neighborNode.containsConnector(opposite(side))) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return false;
    }
}