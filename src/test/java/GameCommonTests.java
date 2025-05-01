/*
 * IJA (Seminář Java): 2024/25 Ukol 1
 * Author:  Radek Kočí, VUT FIT
 * Created: 02/2025
 */

import common.GameNode;
import common.Position;
import common.Side;
import game.Game;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IJA 2024/25, úkol 1, testovací třída,
 * @author Radek Koci <koci AT fit.vut.cz>
 */
public class GameCommonTests {

    /**
     * Test vytvoření prostředí (hry) s prázdnými políčky. 2b.
     */
    @Test
    public void test01() {
        Game game;

        assertThrows(
                IllegalArgumentException.class,
                () -> Game.create("default", -1, 8),
                "Chybne vytvoreni hry, musi generovat vyjimku."
        );

        game = Game.create("default", 15, 10);
        assertEquals(15, game.rows(), "Test spravneho poctu radku.");
        assertEquals(10, game.cols(), "Test spravneho poctu sloupcu.");

        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                GameNode node = game.node(new Position(r, c));
                assertFalse(node.isBulb() || node.isLink() || node.isPower(),
                        "Test spravneho typu policka.");
                for (Side s : Side.values()) {
                    this.checkConnectors(node);
                }
            }
        }
    }

    /**
     * Test vytvoření jednoho typu políčka - žárovka. 2b.
     */
    @Test
    public void test02() {
        Game game;
        GameNode node;

        game = Game.create("default", 15, 10);

        node = game.createBulbNode(new Position(16, 1), Side.NORTH);
        assertNull(node);

        node = game.createBulbNode(new Position(2, 1), Side.NORTH);
        assertFalse(node.isLink() || node.isPower(), "Test spravneho typu policka.");
        assertTrue(node.isBulb(), "Test spravneho typu policka.");
        assertEquals(new Position(2, 1), node.getPosition());
        this.checkConnectors(node, Side.NORTH);

        node = game.createBulbNode(new Position(2, 5), Side.WEST);
        this.checkConnectors(node, Side.WEST);
    }

    /**
     * Test vytvoření jednoho typu políčka - vodič. 2b.
     */
    @Test
    public void test03() {
        Game game;
        GameNode node;

        game = Game.create("default", 15, 10);

        node = game.createLinkNode(new Position(16, 1), Side.NORTH, Side.WEST);
        assertNull(node);

        node = game.createLinkNode(new Position(6, 1), Side.NORTH);
        assertNull(node);

        node = game.createLinkNode(new Position(8, 2), Side.EAST, Side.SOUTH, Side.WEST);
        assertFalse(node.isBulb() || node.isPower(), "Test spravneho typu policka.");
        assertTrue(node.isLink(), "Test spravneho typu policka.");
        assertEquals(new Position(8, 2), node.getPosition());
        this.checkConnectors(node, Side.EAST, Side.SOUTH, Side.WEST);

        node = game.createLinkNode(new Position(3, 2), Side.EAST, Side.SOUTH);
        this.checkConnectors(node, Side.EAST, Side.SOUTH);
    }

    /**
     * Test vytvoření jednoho typu políčka - zdroj. 2b.
     */
    @Test
    public void test04() {
        Game game;
        GameNode node;

        game = Game.create("default", 15, 10);

        node = game.createPowerNode(new Position(16, 1), Side.NORTH);
        assertNull(node);

        node = game.createPowerNode(new Position(6, 1));
        assertNull(node);

        node = game.createPowerNode(new Position(2, 8), Side.NORTH, Side.SOUTH);
        assertFalse(node.isBulb() || node.isLink(), "Test spravneho typu policka.");
        assertTrue(node.isPower(), "Test spravneho typu policka.");
        assertEquals(new Position(2, 8), node.getPosition());
        this.checkConnectors(node, Side.NORTH, Side.SOUTH);

        node = game.createPowerNode(new Position(2, 9), Side.WEST);
        assertNull(node);
    }

    /**
     * Test otáčení políček. 2b.
     */
    @Test
    public void test05() {
        Game game;
        GameNode node;

        game = Game.create("default", 15, 10);
        game.createPowerNode(new Position(2, 8), Side.NORTH, Side.WEST);
        game.createLinkNode(new Position(8, 2), Side.EAST, Side.SOUTH, Side.WEST);
        game.createLinkNode(new Position(12, 8), Side.EAST, Side.WEST);
        game.createBulbNode(new Position(2, 1), Side.NORTH);

        node = game.node(new Position(2, 8));
        this.checkConnectors(node, Side.NORTH, Side.WEST);
        node.turn();
        this.checkConnectors(node, Side.NORTH, Side.EAST);
        node.turn();
        this.checkConnectors(node, Side.EAST, Side.SOUTH);

        node = game.node(new Position(8, 2));
        this.checkConnectors(node, Side.EAST, Side.SOUTH, Side.WEST);
        node.turn();
        this.checkConnectors(node, Side.NORTH, Side.SOUTH, Side.WEST);

        node = game.node(new Position(12, 8));
        this.checkConnectors(node, Side.EAST, Side.WEST);
        node.turn();
        this.checkConnectors(node, Side.NORTH, Side.SOUTH);

        node = game.node(new Position(2, 1));
        this.checkConnectors(node, Side.NORTH);
        node.turn();
        this.checkConnectors(node, Side.EAST);
    }

    private void checkConnectors(GameNode node, Side... sides) {
        List<Side> all = new ArrayList<>(Arrays.asList(Side.values()));
        for (Side s : sides) {
            all.remove(s);
            assertTrue(node.containsConnector(s),
                    "Test spravne obsazenych konektoru policka: " + node + " --> " + s);
        }
        for (Side s : all) {
            assertFalse(node.containsConnector(s),
                    "Test spravne obsazenych konektoru policka: " + node + " --> " + s);
        }
    }
}