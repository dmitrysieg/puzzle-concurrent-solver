package ru.sieg.view;

import ru.sieg.logic.Solver;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class SolverView extends JPanel {

    private Solver solver;
    private SolverLogo solverLogo;
    private int width;
    private int height;

    public SolverView(final Solver solver) {
        put(solver);
        this.setBorder(LineBorder.createBlackLineBorder());
    }

    public void put(Solver solver) {
        this.solver = solver;
        this.solverLogo = SolverLogo.create(7, solver.getName());
    }

    public void updateClip() {

        final AtomicInteger maxX = new AtomicInteger();
        final AtomicInteger maxY = new AtomicInteger();

        solver.getPiecesClusters()
                .forEach(cluster -> {
                    final Rectangle boundingBox = cluster.getBoundingBox();
                    maxX.addAndGet(boundingBox.width);
                    maxY.set(Math.max(maxY.get(), boundingBox.height));
                });

        // 3 = 1 gap + 1 logo + 1 gap.
        this.width = Math.max(maxX.get(), 3 * solverLogo.getImg().getHeight());
        // 2 = 1 logo + 1 gap.
        this.height = maxY.get() + solverLogo.getImg().getHeight() * 2;
        this.setPreferredSize(new Dimension(this.width * Solver.PiecesCluster.IMG_SIZE,
                this.height * Solver.PiecesCluster.IMG_SIZE));

        //System.out.println(this.solver.getName() + "\t\tupdateClip():\t\t" + maxX.get() + "\t\t" + maxY.get());
    }

    @Override
    public void paintComponent(final Graphics g) {

        final BufferedImage logo = solverLogo.getImg();

        final AtomicInteger nextX = new AtomicInteger(this.width / 2);
        nextX.addAndGet(-logo.getWidth() / 2);
        final AtomicInteger nextY = new AtomicInteger(0);

        g.drawImage(logo,
                nextX.get() * Solver.PiecesCluster.IMG_SIZE,
                nextY.get() * Solver.PiecesCluster.IMG_SIZE,
                logo.getWidth() * Solver.PiecesCluster.IMG_SIZE,
                logo.getHeight() * Solver.PiecesCluster.IMG_SIZE,
                null);

        nextX.set(0);
        nextY.addAndGet(logo.getHeight() * 2);

        // 1 -> 2 -> 1
        // 2 -> 3 -> 1
        // 3 -> 4 -> 2
        // 4 -> 5 -> 2
        // 5 -> 6 -> 2
        // ...
        // 8 -> 9 -> 3
        final int rowAmount = (int) Math.sqrt((double) solver.getPiecesClustersSize() + 1);
        // todo calculate rows

        solver.getPiecesClusters().stream()
                .sorted(Comparator.comparingInt(Solver.PiecesCluster::size).reversed())
                .forEachOrdered(pc -> {
                    final BufferedImage clusterImage = pc.toImage();

                    final int nonScaledX = nextX.getAndAdd(clusterImage.getWidth() + 1);
                    final int nonScaledY = nextY.get();

                    g.drawImage(
                            clusterImage,
                            nonScaledX * Solver.PiecesCluster.IMG_SIZE,
                            nonScaledY * Solver.PiecesCluster.IMG_SIZE,
                            clusterImage.getWidth() * Solver.PiecesCluster.IMG_SIZE,
                            clusterImage.getHeight() * Solver.PiecesCluster.IMG_SIZE,
                            null
                    );
                });
    }
}
