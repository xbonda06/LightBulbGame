package logging;

import game.Game;
import common.GameNode;
import common.Position;
import common.Side;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GameSnapshotDto {
    public final int moveNumber;
    public final long timestamp;
    public final int rows, cols;
    public final List<NodeDto> nodes;

    public GameSnapshotDto(Game game, int moveNumber, Instant now) {
        this.moveNumber = moveNumber;
        this.timestamp = now.toEpochMilli();
        this.rows = game.rows();
        this.cols = game.cols();
        this.nodes = new ArrayList<>();

        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                nodes.add(new NodeDto(game.node(new Position(r, c))));
            }
        }
    }

    public static class NodeDto {
        public final int row, col;
        public final boolean isPower, isBulb, isLink, isLit;
        public final List<String> connectors;

        public NodeDto(GameNode node) {
            this.row = node.getPosition().getRow();
            this.col = node.getPosition().getCol();
            this.isPower = node.isPower();
            this.isBulb  = node.isBulb();
            this.isLink  = node.isLink();
            this.isLit   = node.light();
            this.connectors = new ArrayList<>();
            for (Side s : node.getConnectors()) {
                connectors.add(s.name());
            }
        }
    }
}