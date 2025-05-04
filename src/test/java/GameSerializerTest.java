package json;
import game.Game;
import common.Position;
import common.Side;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSerializerTest {

    private Path tempLog;
    private Game game;

    @BeforeEach
    void setUp() throws Exception {
        // create a temp file for our log
        tempLog = Files.createTempFile("lightbulb-log", ".json");

        // create a small 2×2 game
        game = Game.create(2, 2);

        // TODO: redirect the game's internal serializer to tempLog
        // e.g. via a setter:
        //    game.setSerializer(new GameSerializer(tempLog));
        // or via reflection:
        //    Field f = Game.class.getDeclaredField("serializer");
        //    f.setAccessible(true);
        //    f.set(game, new GameSerializer(tempLog));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(tempLog);
    }

    @Test
    void testSerializationSequence() throws Exception {
        // 1) first turn at [1,1]
        game.node(new Position(1, 1)).turn();
        printAndAssertMoveNumber(1);

        // 2) create power at [1,2], then turn it
        game.createPowerNode(new Position(1, 2), Side.SOUTH);
        game.node(new Position(1, 2)).turn();
        printAndAssertMoveNumber(2);

        // 3) create bulb at [2,1], then turn it
        game.createBulbNode(new Position(2, 1), Side.NORTH);
        game.node(new Position(2, 1)).turn();
        printAndAssertMoveNumber(3);

        // 4) create link at [2,2], then turn it
        game.createLinkNode(new Position(2, 2), Side.NORTH, Side.WEST);
        game.node(new Position(2, 2)).turn();
        printAndAssertMoveNumber(4);

        // 5) undo and redo
        game.undo();
        printAndAssertMoveNumber(4, "after undo");   // should re-log current state at moveNumber=4
        game.redo();
        printAndAssertMoveNumber(4, "after redo");
    }

    private void printAndAssertMoveNumber(int expected, String... note) throws Exception {
        String content = Files.readString(tempLog);
        System.out.println((note.length>0? note[0] + ": ":"") + content);

        assertTrue(content.contains("\"moveNumber\": " + expected),
                "Expected moveNumber = " + expected);
    }
}