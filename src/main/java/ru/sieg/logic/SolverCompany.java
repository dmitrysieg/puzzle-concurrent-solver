package ru.sieg.logic;

import ru.sieg.logic.domain.Plane;
import ru.sieg.view.MainView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class SolverCompany {

    private final List<Solver> solvers;

    private final int piecesAmount;
    private final MainView view;

    public SolverCompany(final int amount,
                         final int piecesAmount,
                         final MainView view) {
        this.piecesAmount = piecesAmount;
        this.solvers = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            final Solver solver = new Solver("Solver " + (i + 1), this, view);
            solvers.add(solver);
        }
        this.view = view;
    }

    public Collection<Solver> getSolvers() {
        return solvers;
    }

    public Plane solve(final PieceRepository pieceRepository) {

        final Random random = new Random(System.currentTimeMillis());

        long time = System.currentTimeMillis();

        solvers.forEach(solver -> {
            solver.pointToRepository(pieceRepository);
        });

        double[] priorities = calculateInitialPriorities();

//        int i = 0;
        while (!pieceRepository.getPieces().isEmpty() || sumMaxClusterSize() < piecesAmount) {

            final Solver solver = solvers.get(getRandomIndex(priorities, random));
//            i = (i + 1) % solvers.size();

            solver.doPieceApproach(view);

            priorities = calculatePriorities();
        }

        System.out.println("solve(): " + (System.currentTimeMillis() - time) + " ms");
        return null;
    }

    private double[] calculatePriorities() {

        final double[] result = new double[solvers.size()];
        double totalClustersAtHands = 0.0D;

        for (int j = 0; j < result.length; j++) {
            totalClustersAtHands = totalClustersAtHands + (solvers.get(j).getOwnedPieces() + 1);
        }
        for (int j = 0; j < result.length; j++) {
            result[j] = (solvers.get(j).getOwnedPieces() + 1) / totalClustersAtHands;
        }

        return result;
    }

    private double[] calculateInitialPriorities() {
        final double[] result = new double[solvers.size()];
        for (int j = 0; j < result.length; j++) {
            result[j] = 1.0D / result.length;
        }
        return result;
    }

    private int sumMaxClusterSize() {
        final int max = solvers.stream().map(s -> s.getMaxCluster().map(Solver.PiecesCluster::size).orElse(0)).max(Integer::compareTo).orElse(0);
        System.out.println("=" + max);
        return max;
    }

    private int getRandomIndex(final double[] weights, final Random random) {
        final double[] distr = new double[weights.length];
        double acc = 0.0D;
        for (int i = 0; i < weights.length; i++) {
            distr[i] = acc;
            acc += weights[i];
        }

        double uniform = random.nextDouble();
        for (int i = 0; i < weights.length; i++) {
            if (uniform < distr[i]) {
                return i;
            }
        }
        return weights.length - 1;
    }
}