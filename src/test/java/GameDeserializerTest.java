package json;

import common.Position;
import game.Game;
import json.GameDeserializer;    // <— import YOUR class
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GameDeserializerTest {

    @Test
    void testFullHistoryCombination() throws Exception {
        Path json = Path.of("src","test","resources","sample.json");
        GameDeserializer deser = new GameDeserializer(json);

        List<Position> full = deser.getFullHistory();
        System.out.println("Full history: " +
                full.stream()
                        .map(p -> "(" + p.getRow() + "," + p.getCol() + ")")
                        .collect(Collectors.toList())
        );

        List<Position> expected = List.of(
                new Position(1, 1),
                new Position(1, 2),
                new Position(1, 3),
                new Position(1, 4),
                new Position(1, 5),
                new Position(2, 1),
                new Position(2, 2)
        );
        System.out.println("Expected history: " +
                expected.stream()
                        .map(p -> "(" + p.getRow() + "," + p.getCol() + ")")
                        .collect(Collectors.toList())
        );

        assertEquals(expected, full);
    }

    @Test
    void testGameStepNavigation() throws Exception {
        Path json = Path.of("src","test","resources","sample.json");
        GameDeserializer deser = new GameDeserializer(json);
        Game game = deser.getGame();

        assertEquals(0, deser.getCurrentStep());
        assertTrue(deser.nextStep());
        assertEquals(1, deser.getCurrentStep());
        assertTrue(deser.previousStep());
        assertEquals(0, deser.getCurrentStep());
    }
}