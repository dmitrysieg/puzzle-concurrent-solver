package ru.sieg.logic.domain.bhvr;

import ru.sieg.logic.Solver;
import ru.sieg.logic.SolverCompany;

import java.util.List;
import java.util.Random;

public class SolverChooseSimpleRandomStrategy implements SolverChooseStrategy {

    private Random random;

    public SolverChooseSimpleRandomStrategy(final Random random) {
        this.random = random;
    }

    @Override
    public Solver getNextSolver(final SolverCompany solverCompany) {
        final List<Solver> solvers = solverCompany.getSolvers();
        return solvers.get(random.nextInt(solvers.size()));
    }
}
