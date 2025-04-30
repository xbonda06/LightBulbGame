import common.Position;
import game.Game;
import common.GameNode;
import common.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameUndoRedoTest {

    /**
     * Utility: serialises the board into a string so snapshots can be compared easily.
     */
    private String dump(Game g) {
        StringBuilder sb = new StringBuilder();
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                var n = g.node(new Position(r, c));

                // Encode each node uniquely so rotations always change the string
                if (n.isPower()) {
                    sb.append('P');
                } else if (n.isBulb()) {
                    // Encode bulb by the side of its single connector: N,E,S,W
                    Side s = n.getConnectors().get(0);
                    sb.append(s.name().charAt(0)); // N/E/S/W
                } else {
                    // For link nodes encode the four sides as bits NESW -> 0‑15
                    int code = 0;
                    if (n.containsConnector(Side.NORTH)) code |= 1;
                    if (n.containsConnector(Side.EAST)) code |= 2;
                    if (n.containsConnector(Side.SOUTH)) code |= 4;
                    if (n.containsConnector(Side.WEST)) code |= 8;
                    sb.append((char) ('a' + code)); // 'a'..'p'
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Test
    void undoRedoRestoresBoard() {
        // 1. Create a small 3×3 game
        Game g = Game.generate(3, 3);
        g.randomizeRotations();          // shuffle rotations
        g.commitMove();                  // ---- snapshot #1 (randomised)

        // 2. Make a move: rotate the first rotatable cell we find (non‑power)
        Position rot = null;
        outer:
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                GameNode n = g.node(new Position(r, c));
                if (!n.isPower()) {        // power node shouldn't be rotated
                    rot = new Position(r, c);
                    break outer;
                }
            }
        }
        assertNotNull(rot, "No rotatable cell found");
        g.node(rot).turn();

        g.commitMove();                  // ---- snapshot #2 (after move)

        String afterMove = dump(g);      // save board snapshot

        // 3. Undo
        assertTrue(g.undo(), "undo() should return true");
        String afterUndo = dump(g);

        // a) the board after undo must differ from the board after the move
        assertNotEquals(afterMove, afterUndo, "Board should revert to the previous state");

        // b) a second undo is impossible (only the initial snapshot remains)
        assertFalse(g.undo(), "Nothing left to undo");

        // 4. Redo
        assertTrue(g.redo(), "redo() should return true");
        String afterRedo = dump(g);

        // c) After redo, the board must look exactly as it did after the first move
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
        g.commitMove();                           // snapshot0

        java.util.List<String> snap = new java.util.ArrayList<>();
        snap.add(dump(g));                        // 0

        java.util.Set<Position> used = new java.util.HashSet<>();

        for (int k = 1; k <= 3; k++) {
            Position p = firstRotatable(g, used);
            used.add(p);
            g.node(p).turn();
            g.commitMove();
            snap.add(dump(g));                    // 1,2,3
        }

        for (int idx = 3; idx >= 1; idx--) {
            assertTrue(g.undo(), "Undo #" + idx + " should succeed");
            assertEquals(snap.get(idx - 1), dump(g), "Undo should restore snapshot" + (idx - 1));
        }
        // nothing more to undo
        assertFalse(g.undo(), "Further undo should fail at the beginning");

        // ---------- redo forward to snapshot3 ----------
        for (int idx = 1; idx <= 3; idx++) {
            assertTrue(g.redo(), "Redo #" + idx + " should succeed");
            assertEquals(snap.get(idx), dump(g), "Redo should restore snapshot" + idx);
        }
        // nothing more to redo
        assertFalse(g.redo(), "Further redo should fail at the end");
    }
    /** Encode a raw GameNode matrix using the same scheme as {@link #dump(Game)}. */
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
        g.commitMove();                       // snapshot0
        String snapshot0 = dump(g);

        // make a single move
        Position p = firstRotatable(g, java.util.Set.of());
        g.node(p).turn();
        g.commitMove();

        // undo that move
        assertTrue(g.undo());

        // board returned to snapshot0
        assertEquals(snapshot0, dump(g));

        // --- inspect top of undoStack via reflection ---
        java.lang.reflect.Field fUndo = Game.class.getDeclaredField("undoStack");
        fUndo.setAccessible(true);
        java.util.Stack<?> undo = (java.util.Stack<?>) fUndo.get(g);

        Object state = undo.peek();  // this is Game.GameState
        java.lang.reflect.Field fSnap = state.getClass().getDeclaredField("snapshot");
        fSnap.setAccessible(true);
        GameNode[][] snapMatrix = (GameNode[][]) fSnap.get(state);

        String encodedFromStack = dump(snapMatrix, g.rows(), g.cols());
        assertEquals(snapshot0, encodedFromStack,
                "Snapshot stored in undoStack must equal the recorded snapshot0");
    }

    /** Prints the board in an easy‑to‑read form: each cell lists its type and connectors. */
    private void prettyPrint(Game g) {
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                GameNode n = g.node(new Position(r, c));
                String token;
                if (n.isPower()) {
                    token = "P";
                } else if (n.isBulb()) {
                    token = "B" + n.getConnectors().get(0).name().charAt(0); // e.g. BN
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Side s : Side.values()) {
                        if (n.containsConnector(s)) sb.append(s.name().charAt(0));
                    }
                    token = sb.isEmpty() ? "." : sb.toString();
                }
                // pad to width 4 for alignment
                System.out.printf("%-4s", token);
            }
            System.out.println();
        }
    }

    @Test
    void printStackStates() throws Exception {
        Game g = Game.generate(3, 3);
        g.randomizeRotations();
        g.commitMove();   // snapshot0

        // reflection handles
        java.lang.reflect.Field fUndo = Game.class.getDeclaredField("undoStack");
        java.lang.reflect.Field fRedo = Game.class.getDeclaredField("redoStack");
        fUndo.setAccessible(true);
        fRedo.setAccessible(true);

        java.util.Stack<?> undo = (java.util.Stack<?>) fUndo.get(g);
        java.util.Stack<?> redo = (java.util.Stack<?>) fRedo.get(g);

        // helper lambda to dump top of a stack
        java.util.function.Function<Object,String> topSnapshot =
            state -> {
                try {
                    java.lang.reflect.Field fSnap = state.getClass().getDeclaredField("snapshot");
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

        // ------- make a move -------
        Position p = firstRotatable(g, java.util.Set.of());
        g.node(p).turn();
        g.commitMove();

        System.out.println("\n=== After move & commit ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);

        // ------- undo -------
        g.undo();
        System.out.println("\n=== After undo ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);

        // ------- redo -------
        g.redo();
        System.out.println("\n=== After redo ===");
        System.out.printf("undo size=%d, redo size=%d%n", undo.size(), redo.size());
        prettyPrint(g);
    }
}