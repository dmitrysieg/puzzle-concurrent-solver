package ru.sieg;

import ru.sieg.logic.*;
import ru.sieg.logic.domain.Plane;
import ru.sieg.view.MainView;

public class Main {

    public static void main(String[] args) {

        final MainView view = MainView.create();

        final Plane plane = new PlaneLoader().load("puzzle.jpg");

        final PieceRepository pieceRepository = PlaneTearer.tear(plane);
        view.put(pieceRepository);

        final SolverCompany solverCompany = new SolverCompany(10, plane.getPiecesAmount(), view);
        view.put(solverCompany);

        final Plane resultPlane = solverCompany.solve(pieceRepository);

        final boolean result = PlaneVerifier.matches(plane, resultPlane);

        System.out.println("Result: planes " + (result ? "match" : "do not match"));
    }
}
