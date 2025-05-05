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

}