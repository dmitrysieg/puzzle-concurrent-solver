package ru.sieg.logic;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SnapshotController {

    public void takeSnapshot(final List<Solver> solvers) {

//        final List<Rectangle> boundingRect = getBoundingRects(solvers);
//
//        final Rectangle concludingRect =
    }

    private List<Rectangle> getBoundingRects(List<Solver> solvers) {

        return solvers.stream()
                .flatMap(s -> s.getPiecesClusters().stream())
                .map(Solver.PiecesCluster::getBoundingBox)
                .collect(Collectors.toList());
    }

    private Rectangle getConcluding(final List<Rectangle> rectangles) {
        return null;
    }

    private int[] getMinRectNumbers(final int n) {

        final double sq = Math.sqrt(n);
        final int sq_approx = (int) sq;

        int side_b = (sq_approx * sq_approx < n) ? sq_approx + 1 : sq_approx;
        return new int[]{sq_approx, side_b};
    }
}
