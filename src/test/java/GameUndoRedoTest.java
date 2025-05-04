package game;

import common.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GameUndoRedoAdvancedTest {

    private Game g;
    private Stack<Position> undo;
    private Stack<Position> redo;

    @BeforeEach
    void setUp() throws Exception {
        g = Game.create(3, 3);
        undo = getStack("undoStack");
        redo = getStack("redoStack");
    }

    @Test
    void testSequentialMovesAndUndoAllRedoAll() throws Exception {
        // Provedeme 5 tahů: (1,1), (1,2), (2,2), (3,3), (2,1)
        Position[] moves = {
                new Position(1,1), new Position(1,2),
                new Position(2,2), new Position(3,3),
                new Position(2,1)
        };
        for (Position p : moves) {
            // místo makeMove(p):
            g.node(p).turn();

            assertEquals(p, undo.peek(), "Top of undoStack after move " + p);
            assertTrue(redo.isEmpty(), "Redo must be empty immediately after a new move");
        }
        assertEquals(moves.length, undo.size());
        print("After moves", undo, redo);

        // Undo všechno
        for (int i = moves.length - 1; i >= 0; i--) {
            Position expected = moves[i];
            assertTrue(g.undo(), "Undo should succeed at step " + i);
            assertEquals(expected, redo.peek(), "Top of redoStack after undo");
        }
        assertTrue(undo.isEmpty(), "undoStack must be empty after undo-all");
        print("After undo-all", undo, redo);

        // Redo všechno
        for (int i = 0; i < moves.length; i++) {
            Position expected = moves[i];
            assertTrue(g.redo(), "Redo should succeed at step " + i);
            assertEquals(expected, undo.peek(), "Top of undoStack after redo");
        }
        assertTrue(redo.isEmpty(), "redoStack must be empty after redo-all");
        print("After redo-all", undo, redo);
    }

    @Test
    void testInterleavedUndoRedoAndNewMovesClearsRedo() throws Exception {
        // 1) Dva tahy
        Position a = new Position(1,1);
        Position b = new Position(2,2);
        g.node(a).turn();
        g.node(b).turn();
        print("After two moves", undo, redo);

        // 2) Jedno undo – b → redo
        assertTrue(g.undo());
        print("After undo b", undo, redo);
        assertEquals(b, redo.peek());

        // 3) Nový tah c = (3,3) – musí vymazat redo
        Position c = new Position(3,3);
        g.node(c).turn();
        print("After new move c", undo, redo);
        assertTrue(redo.isEmpty(), "Redo must be cleared after a new move");
        assertEquals(c, undo.peek());

        // 4) Undo c, a
        assertTrue(g.undo());
        assertTrue(g.undo());
        print("After undo c and a", undo, redo);
        assertEquals(a, redo.peek());

        // 5) Další undo neprojde, redo size zůstává
        assertFalse(g.undo());
    }

    @Test
    void testRandomSequenceOfMovesUndosAndRedos() throws Exception {
        // Vygenerujeme náhodných 10 operací: M=move, U=undo, R=redo
        Random rnd = new Random(42);
        List<Position> history = new java.util.ArrayList<>();
        for (int step = 0; step < 10; step++) {
            int op = rnd.nextInt(3);
            if (op == 0) {            // move
                Position p = new Position(rnd.nextInt(3)+1, rnd.nextInt(3)+1);
                g.node(p).turn();
                history.add(p);
                // po novém tahu je redo vymazán
                assertTrue(redo.isEmpty());
            } else if (op == 1) {     // undo
                boolean can = !undo.isEmpty();
                assertEquals(can, g.undo());
            } else {                  // redo
                boolean can = !redo.isEmpty();
                assertEquals(can, g.redo());
            }
            // invarianty můžete tisknout, pokud chcete:
            // print("Step "+step, undo, redo);
        }
    }

    // --- pomocné metody ---

    @SuppressWarnings("unchecked")
    private Stack<Position> getStack(String name) throws Exception {
        Field f = Game.class.getDeclaredField(name);
        f.setAccessible(true);
        return (Stack<Position>) f.get(g);
    }

    private void print(String phase, Stack<Position> undo, Stack<Position> redo) {
        System.out.printf("%-25s undo=%-15s redo=%s%n",
                phase,
                coords(undo), coords(redo));
    }

    private String coords(Stack<Position> s) {
        return s.stream()
                .map(p -> "(" + p.getRow() + "," + p.getCol() + ")")
                .collect(Collectors.joining(",", "[", "]"));
    }
}