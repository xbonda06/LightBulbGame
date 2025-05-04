import common.Position;
import game.Game;
import common.GameNode;
import common.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameUndoRedoTest {

    private String dump(Game g) {
        StringBuilder sb = new StringBuilder();
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                var n = g.node(new Position(r, c));
                if (n.isPower()) {
                    sb.append('P');
                } else if (n.isBulb()) {
                    Side s = n.getConnectors().get(0);
                    sb.append(s.name().charAt(0));
                } else {
                    int code = 0;
                    if (n.containsConnector(Side.NORTH)) code |= 1;
                    if (n.containsConnector(Side.EAST))  code |= 2;
                    if (n.containsConnector(Side.SOUTH)) code |= 4;
                    if (n.containsConnector(Side.WEST))  code |= 8;
                    sb.append((char) ('a' + code));
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Test
    void undoRedoRestoresBoard() {
        Game g = Game.generate(3, 3);
        g.randomizeRotations();

        Position rot = null;
        outer:
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                GameNode n = g.node(new Position(r, c));
                if (!n.isPower()) {
                    rot = new Position(r, c);
                    break outer;
                }
            }
        }
        assertNotNull(rot, "No rotatable cell found");

        g.node(rot).turn();

        String afterMove = dump(g);

        assertTrue(g.undo(), "undo() should return true");
        String afterUndo = dump(g);
        assertNotEquals(afterMove, afterUndo, "Board should revert to the previous state");
        assertFalse(g.undo(), "Nothing left to undo");

        assertTrue(g.redo(), "redo() should return true");
        String afterRedo = dump(g);
        assertEquals(afterMove, afterRedo, "redo() should fully restore the changes");
    }

    private Position firstRotatable(Game g, java.util.Set<Position> forbidden) {
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                Position p = new Position(r, c);
                if (!forbidden.contains(p) && !g.node(p).isPower()) {
                    return p;
                }
            }
        }
        throw new IllegalStateException("No rotatable cell found");
    }

    @Test
    void undoAllThenRedoAll() {
        Game g = Game.generate(4, 4);
        g.randomizeRotations();

        java.util.List<String> snap = new java.util.ArrayList<>();
        snap.add(dump(g));                        // 0

        java.util.Set<Position> used = new java.util.HashSet<>();

        for (int k = 1; k <= 3; k++) {
            Position p = firstRotatable(g, used);
            used.add(p);
            g.node(p).turn();
            snap.add(dump(g));                    // 1,2,3
        }

        for (int idx = 3; idx >= 1; idx--) {
            assertTrue(g.undo(), "Undo #" + idx + " should succeed");
            assertEquals(snap.get(idx - 1), dump(g), "Undo should restore snapshot" + (idx - 1));
        }
        assertFalse(g.undo(), "Further undo should fail at the beginning");

        for (int idx = 1; idx <= 3; idx++) {
            assertTrue(g.redo(), "Redo #" + idx + " should succeed");
            assertEquals(snap.get(idx), dump(g), "Redo should restore snapshot" + idx);
        }
        assertFalse(g.redo(), "Further redo should fail at the end");
    }

    private String dump(GameNode[][] board, int rows, int cols) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                GameNode n = board[r][c];
                if (n.isPower()) {
                    sb.append('P');
                } else if (n.isBulb()) {
                    Side s = n.getConnectors().get(0);
                    sb.append(s.name().charAt(0));
                } else {
                    int code = 0;
                    if (n.containsConnector(Side.NORTH)) code |= 1;
                    if (n.containsConnector(Side.EAST))  code |= 2;
                    if (n.containsConnector(Side.SOUTH)) code |= 4;
                    if (n.containsConnector(Side.WEST))  code |= 8;
                    sb.append((char) ('a' + code));
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Test
    void snapshotOnUndoStackMatchesRecorded() throws Exception {
        Game g = Game.generate(3, 3);
        g.randomizeRotations();
        String snapshot0 = dump(g);

        Position p = firstRotatable(g, java.util.Set.of());
        g.node(p).turn();

        assertTrue(g.undo());
        assertEquals(snapshot0, dump(g));

        var fUndo = Game.class.getDeclaredField("undoStack");
        fUndo.setAccessible(true);

        java.util.Stack<?> undo = (java.util.Stack<?>) fUndo.get(g);

        Object state = undo.peek();
        var fSnap = state.getClass().getDeclaredField("snapshot");
        fSnap.setAccessible(true);
        GameNode[][] snapMatrix = (GameNode[][]) fSnap.get(state);

        String encodedFromStack = dump(snapMatrix, g.rows(), g.cols());
        assertEquals(snapshot0, encodedFromStack,
                "Snapshot stored in undoStack must equal the recorded snapshot0");
    }

    private void prettyPrint(Game g) {
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                GameNode n = g.node(new Position(r, c));
                String token;
                if (n.isPower()) {
                    token = "P";
                } else if (n.isBulb()) {
                    token = "B" + n.getConnectors().get(0).name().charAt(0);
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Side s : Side.values()) {
                        if (n.containsConnector(s)) sb.append(s.name().charAt(0));
                    }
                    token = sb.isEmpty() ? "." : sb.toString();
                }
                System.out.printf("%-4s", token);
            }
            System.out.println();
        }
    }

    @Test
    void printStackStates() throws Exception {
        Game g = Game.generate(3, 3);
        g.randomizeRotations();

        var fUndo = Game.class.getDeclaredField("undoStack");
        var fRedo = Game.class.getDeclaredField("redoStack");
        fUndo.setAccessible(true);
        fRedo.setAccessible(true);

        java.util.Stack<?> undo = (java.util.Stack<?>) fUndo.get(g);
        java.util.Stack<?> redo = (java.util.Stack<?>) fRedo.get(g);

        var topSnapshot = (java.util.function.Function<Object,String>) state -> {
            try {
                var fSnap = state.getClass().getDeclaredField("snapshot");
                fSnap.setAccessible(true);
                GameNode[][] matrix = (GameNode[][]) fSnap.get(state);
                return dump(matrix, g.rows(), g.cols()).trim();
            } catch (Exception e) {
                return "<error>";
            }
        };

        System.out.println("=== After init ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);

        Position p = firstRotatable(g, java.util.Set.of());
        g.node(p).turn();

        System.out.println("\n=== After move & commit ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);

        g.undo();
        System.out.println("\n=== After undo ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);

        g.redo();
        System.out.println("\n=== After redo ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);
    }
}