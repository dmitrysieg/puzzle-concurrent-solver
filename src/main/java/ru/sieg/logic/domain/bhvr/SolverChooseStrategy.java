package ru.sieg.logic.domain.bhvr;

import ru.sieg.logic.Solver;
import ru.sieg.logic.SolverCompany;

public interface SolverChooseStrategy {

    Solver getNextSolver(final SolverCompany solverCompany);
}
