package json;

import common.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDeserializerIntegrationTest {

    @Test
    void latestSave_allowsFullUndoRedoTraversal() {
        // discover all saved IDs
        List<Integer> ids = GameArchive.listSavedGameIds();
        assertFalse(ids.isEmpty(), "No saved games found in data/");

        // pick the latest
        int lastId = ids.get(ids.size() - 1);
        GameDeserializer deser = GameArchive.load(lastId);

        int totalSteps = deser.getTotalSteps();
        assertTrue(totalSteps > 0, "Loaded save #" + lastId + " has no moves?");

        // forward traversal
        for (int step = 1; step <= totalSteps; step++) {
            assertTrue(deser.nextStep(), "nextStep() should succeed at step " + (step - 1));
            assertEquals(step, deser.getCurrentStep());
        }
        assertFalse(deser.nextStep(), "nextStep() should return false past last step");

        // backward traversal
        for (int step = totalSteps - 1; step >= 0; step--) {
            assertTrue(deser.previousStep(), "previousStep() should succeed at step " + (step + 1));
            assertEquals(step, deser.getCurrentStep());
        }
        assertFalse(deser.previousStep(), "previousStep() should return false at step 0");
    }

    @Test
    void game_lastTurnedNode_tracksHistory() {
        List<Integer> ids = GameArchive.listSavedGameIds();
        int lastId = ids.get(ids.size() - 1);
        GameDeserializer deser = GameArchive.load(lastId);
        var history = deser.getFullHistory();

        // make sure history actually loaded
        assertFalse(history.isEmpty(), "History empty in save #" + lastId);

        // step forward twice, assert lastTurnedNode matches
        deser.nextStep();
        Position p1 = history.get(0);
        assertEquals(p1, deser.getGame().getLastTurnedNode(),
                "After 1st nextStep, lastTurnedNode must be " + p1);

        if (history.size() > 1) {
            deser.nextStep();
            Position p2 = history.get(1);
            assertEquals(p2, deser.getGame().getLastTurnedNode(),
                    "After 2nd nextStep, lastTurnedNode must be " + p2);
        }
    }
}