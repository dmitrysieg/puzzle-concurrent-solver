package ru.sieg;

import ru.sieg.logic.*;
import ru.sieg.logic.domain.Plane;
import ru.sieg.view.MainView;

public class Main {

    private static final int HORIZONTAL_SIZE = 320;
    private static final int VERTICAL_SIZE = 240;

    public static void main(String[] args) {

        final MainView view = MainView.create();

        final Plane plane = new Plane(HORIZONTAL_SIZE, VERTICAL_SIZE).initValues();

        final PieceRepository pieceRepository = PlaneTearer.tear(plane);

        final SolverCompany solverCompany = new SolverCompany(10, HORIZONTAL_SIZE * VERTICAL_SIZE, view);

        final Plane resultPlane = solverCompany.solve(pieceRepository);

        final boolean result = PlaneVerifier.matches(plane, resultPlane);

        System.out.println("Result: planes " + (result ? "match" : "do not match"));
    }
}
