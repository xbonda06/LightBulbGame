package game;

import common.Position;
import common.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameUndoRedoStateChangeTest {

    @Test
    void testUndoRedoActuallyChangesBoardStateWithVerbosePrints() throws Exception {
        // 1) Vygeneruj hru 3×3 (obsahuje spojení a žárovky)
        Game g = Game.generate(3, 3);

        // 2) Najdi buňku, která není power a má alespoň jeden konektor
        Position movePos = null;
        outer:
        for (int r = 1; r <= g.rows(); r++) {
            for (int c = 1; c <= g.cols(); c++) {
                var node = g.node(new Position(r, c));
                if (!node.isPower() && !node.getConnectors().isEmpty()) {
                    movePos = new Position(r, c);
                    break outer;
                }
            }
        }
        assertNotNull(movePos, "Musí existovat pozice s konektory");

        // 3) Verbosní tisk před tahem
        System.out.println("===== Board before move =====");
        verbosePrint(g);

        // 4) Proveď tah (otočení) na vybrané pozici
        System.out.printf("Performing turn() on position: (%d,%d)%n",
                movePos.getRow(), movePos.getCol());
        g.node(movePos).turn();

        // 5) Verbosní tisk po tahu
        System.out.println("===== Board after move =====");
        verbosePrint(g);

        // 6) Undo a verbose tisk
        assertTrue(g.undo(), "undo() musí vrátit true");
        System.out.println("===== Board after undo =====");
        verbosePrint(g);

        // 7) Redo a verbose tisk
        assertTrue(g.redo(), "redo() musí vrátit true");
        System.out.println("===== Board after redo =====");
        verbosePrint(g);
    }

    /** Verbosně vypíše každý řádek a buňku s popiskem */
    private void verbosePrint(Game g) {
        for (int r = 1; r <= g.rows(); r++) {
            StringBuilder line = new StringBuilder();
            for (int c = 1; c <= g.cols(); c++) {
                var n = g.node(new Position(r, c));
                String desc;
                if (n.isPower()) {
                    desc = "Power";
                } else if (n.isBulb()) {
                    desc = "Bulb(" + n.getConnectors().get(0).name() + ")";
                } else if (n.getConnectors().isEmpty()) {
                    desc = "Empty";
                } else {
                    desc = "Link" + n.getConnectors().toString();
                }
                // přidáme souřadnice pro přehlednost
                line.append(String.format("(%d,%d):%s", r, c, desc));
                if (c < g.cols()) line.append(" | ");
            }
            System.out.println(line);
        }
    }
}