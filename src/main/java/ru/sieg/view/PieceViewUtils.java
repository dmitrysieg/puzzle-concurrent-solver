package ru.sieg.view;

import ru.sieg.logic.Solver;
import ru.sieg.logic.domain.Piece;

import java.awt.*;

public class PieceViewUtils {

    public static void draw(final Graphics g,
                            final Piece piece,
                            final int x,
                            final int y) {
        g.setColor(new Color(piece.getValue()));
        g.fillRect(
                x * Solver.PiecesCluster.IMG_SIZE,
                y * Solver.PiecesCluster.IMG_SIZE,
                Solver.PiecesCluster.IMG_SIZE,
                Solver.PiecesCluster.IMG_SIZE);
    }
}
