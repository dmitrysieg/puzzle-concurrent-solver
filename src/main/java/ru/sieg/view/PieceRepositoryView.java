package ru.sieg.view;

import ru.sieg.logic.PieceRepository;
import ru.sieg.logic.Solver;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PieceRepositoryView extends JPanel {

    private PieceRepository pieceRepository;
    private int dimension;

    public PieceRepositoryView(final PieceRepository pieceRepository) {
        super();
        this.pieceRepository = pieceRepository;

        dimension = (int) Math.sqrt(pieceRepository.size());
        this.setPreferredSize(new Dimension((dimension + 1) * Solver.PiecesCluster.IMG_SIZE,
                (dimension + 1) * Solver.PiecesCluster.IMG_SIZE));
        this.setMaximumSize(this.getPreferredSize());

        this.setBorder(LineBorder.createBlackLineBorder());
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        synchronized (pieceRepository) {
            final AtomicInteger x = new AtomicInteger();
            final AtomicInteger y = new AtomicInteger();
            pieceRepository.stream().forEach(piece -> {
                PieceViewUtils.draw(g, piece, x.get(), y.get());
                x.incrementAndGet();
                if (x.get() >= dimension) {
                    x.set(0);
                    y.incrementAndGet();
                }
            });
        }
    }
}
