package ru.sieg.logic.domain.bhvr;

import ru.sieg.logic.Solver;
import ru.sieg.logic.SolverCompany;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SolverChooseSequentalStrategy implements SolverChooseStrategy {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Solver getNextSolver(final SolverCompany solverCompany) {
        final List<Solver> solvers = solverCompany.getSolvers();
        final int next = counter.getAndUpdate(i -> (i + 1) % solvers.size());
        return solvers.get(next);
    }
}
