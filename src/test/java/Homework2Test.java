/*
 * IJA (Seminář Java): 2024/25 Ukol 2
 * Author:  Radek Kočí, VUT FIT
 * Created: 03/2025
 */

import common.GameNode;
import common.Position;
import common.Side;
import static common.Side.EAST;
import static common.Side.NORTH;
import static common.Side.SOUTH;
import static common.Side.WEST;
import game.Game;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Radek Koci <koci AT fit.vut.cz>
 */
public class Homework2Test {

    private Game game;

    private final Object def[][]= {
            {"L", 4, 5, NORTH, EAST, SOUTH},
            {"L", 5, 5, NORTH, EAST, WEST},
            {"L", 5, 4, EAST, SOUTH},
            {"L", 4, 6, EAST, SOUTH},
            {"L", 5, 6, NORTH, SOUTH},
            {"L", 3, 6, EAST, WEST},
            {"L", 3, 4, EAST, WEST},
            {"L", 5, 7, EAST, SOUTH},
            {"B", 6, 4, NORTH},
            {"B", 3, 3, NORTH},
            {"B", 2, 6, SOUTH},
            {"B", 4, 7, WEST},
            {"P", 3, 5, EAST, SOUTH}
    };

    @BeforeEach
    public void setUp() {
        game = Game.create("default", 10, 12);

        for (Object[] n : def) {
            String type = (String)n[0];
            int row = (Integer)n[1];
            int col = (Integer)n[2];
            Position p = new Position(row, col);
            Side sides[] = new Side[n.length-3];
            for (int i = 3; i < n.length; i++) {
                sides[i-3] = (Side)n[i];
            }
            switch (type) {
                case "L" -> game.createLinkNode(p, sides);
                case "B" -> game.createBulbNode(p, sides[0]);
                case "P" -> game.createPowerNode(p, sides);
            }
        }

        game.init();
    }

    @Test
    public void test01() {
        Integer lights[][]= {
                {3, 5},
                {4, 5},
                {5, 5},
                {3, 6},
                {5, 4},
                {6, 4}
        };
        this.testLight(lights);
    }

    @Test
    public void test02() {
        for (Object[] n : def) {
            String type = (String)n[0];
            int row = (Integer)n[1];
            int col = (Integer)n[2];
            Position p = new Position(row, col);
            StringBuilder sides = new StringBuilder();
            for (int i = 3; i < n.length; i++) {
                sides.append(n[i].toString());
                if (i+1 < n.length) sides.append(",");
            }
            GameNode node = game.node(p);
            assertEquals("{" + type + "["+row+"@"+col+"][" + sides + "]}",
                    node.toString(),
                    "Test spravne reprezentace stavu policka.");
        }
    }

    @Test
    public void test03() {
        Position pos = new Position(3, 5);
        GameNode node = game.node(pos);
        node.turn();

        Integer lights[][]= {
                {3, 5},
                {4, 5},
                {5, 5},
                {3, 4},
                {5, 4},
                {6, 4}
        };
        this.testLight(lights);
    }

    @Test
    public void test04() {
        Position pos = new Position(3, 5);
        GameNode node = game.node(pos);
        assertEquals("{P[3@5][EAST,SOUTH]}", node.toString(),
                "Test spravne reprezentace stavu policka.");
        node.turn();

        for (Object[] n : def) {
            String type = (String)n[0];
            int row = (Integer)n[1];
            int col = (Integer)n[2];
            Position p = new Position(row, col);
            node = game.node(p);
            if (p.equals(pos)) {
                assertEquals("{P[3@5][SOUTH,WEST]}",
                        node.toString(),
                        "Test spravne reprezentace stavu policka.");
                continue;
            }
            StringBuilder sides = new StringBuilder();
            for (int i = 3; i < n.length; i++) {
                sides.append(n[i].toString());
                if (i+1 < n.length) sides.append(",");
            }
            assertEquals("{" + type + "["+row+"@"+col+"][" + sides + "]}",
                    node.toString(),
                    "Test spravne reprezentace stavu policka.");
        }
    }

    @Test
    public void test05() {
        Position pos1 = new Position(3, 5);
        game.node(pos1).turn();
        Position pos2 = new Position(5, 5);
        game.node(pos2).turn();
        Position pos3 = new Position(5, 6);
        game.node(pos3).turn();
        Position pos4 = new Position(3, 3);
        game.node(pos4).turn();

        Integer lights[][]= {
                {3, 5},
                {4, 5},
                {5, 5},
                {5, 6},
                {3, 4},
                {3, 3}
        };
        this.testLight(lights);

        assertEquals("{P[3@5][SOUTH,WEST]}", game.node(pos1).toString(),
                "Test spravne reprezentace stavu policka.");
        assertEquals("{L[5@5][NORTH,EAST,SOUTH]}", game.node(pos2).toString(),
                "Test spravne reprezentace stavu policka.");
        assertEquals("{L[5@6][EAST,WEST]}", game.node(pos3).toString(),
                "Test spravne reprezentace stavu policka.");
        assertEquals("{B[3@3][EAST]}", game.node(pos4).toString(),
                "Test spravne reprezentace stavu policka.");
    }

    private void testLight(Integer[][] lights) {
        List<Position> allpos = new ArrayList<>();
        for (int r = 1; r <= game.rows(); r++) {
            for (int c = 1; c <= game.cols(); c++) {
                allpos.add(new Position(r, c));
            }
        }

        for (Integer[] coord : lights) {
            Position pos = new Position(coord[0], coord[1]);
            allpos.remove(pos);
            assertTrue(game.node(pos).light(),
                    "Políčko " + pos + " má být pod proudem.");
        }

        for (Position pos : allpos) {
            assertFalse(game.node(pos).light(),
                    "Políčko " + pos + " nemá být pod proudem.");
        }
    }
}