package ru.sieg.logic;

import ru.sieg.logic.domain.Plane;
import ru.sieg.logic.domain.bhvr.SolverChooseSequentalStrategy;
import ru.sieg.logic.domain.bhvr.SolverChooseSimpleRandomStrategy;
import ru.sieg.logic.domain.bhvr.SolverChooseStrategy;
import ru.sieg.view.MainView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SolverCompany {

    private final List<Solver> solvers;
    private SolverChooseStrategy solverChooseStrategy;

    private final int piecesAmount;
    private final MainView view;
    private final SnapshotController snapshotController;

    private boolean paused;

    public SolverCompany(final int amount,
                         final int piecesAmount,
                         final MainView view) {

        //final Random random = new Random(System.currentTimeMillis());
        //solverChooseStrategy = new SolverChooseSimpleRandomStrategy(random);
        solverChooseStrategy = new SolverChooseSequentalStrategy();

        this.piecesAmount = piecesAmount;
        this.solvers = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            final Solver solver = new Solver("Solver " + (i + 1), this, view);
            solvers.add(solver);
        }
        this.view = view;
        this.snapshotController = new SnapshotController();
    }

    public void takeClustersSnapshot() {
        snapshotController.takeSnapshot(solvers);
    }

    public List<Solver> getSolvers() {
        return solvers;
    }

    public void togglePause() {
        paused = !paused;
    }

    public Plane solve(final PieceRepository pieceRepository) {

        long time = System.currentTimeMillis();

        solvers.forEach(solver -> {
            solver.pointToRepository(pieceRepository);
        });

        while (!pieceRepository.isEmpty() || sumMaxClusterSize() < piecesAmount) {

            while (paused) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final Solver solver = solverChooseStrategy.getNextSolver(this);
            solver.doPieceApproach(view);
        }

        System.out.println("[Company]\t\tsolve(): " + (System.currentTimeMillis() - time) + " ms");
        return solvers.stream()
                .filter(s -> !s.getPiecesClusters().isEmpty())
                .findFirst()
                .map(solver -> solver.getPiecesClusters().get(0))
                .map(Solver.PiecesCluster::toPlane)
                .orElse(null);
    }

    private int sumMaxClusterSize() {
        return solvers.stream()
                .map(s -> s.getMaxCluster().map(Solver.PiecesCluster::size).orElse(0))
                .max(Integer::compareTo)
                .orElse(0);
    }
}
