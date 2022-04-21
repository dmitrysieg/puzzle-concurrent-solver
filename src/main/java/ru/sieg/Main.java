package ru.sieg;

import ru.sieg.logic.*;
import ru.sieg.logic.domain.Plane;
import ru.sieg.view.MainView;
import ru.sieg.view.threadview.SvgThreadVizualizer;

public class Main {

    public static void main(String[] args) {

        final MainView view = MainView.create();

        final Plane plane = new PlaneLoader().load("puzzle.jpg");

        final PieceRepository pieceRepository = PlaneTearer.tear(plane);
        view.put(pieceRepository);

        final SolverCompany solverCompany = new SolverCompany(10, plane.getPiecesAmount(), view);
        view.put(solverCompany);

        final SvgThreadVizualizer threadVizualizer = new SvgThreadVizualizer("g:\\puzzle.svg");
        threadVizualizer.threadStart(Thread.currentThread());
        final Plane resultPlane = solverCompany.solve(pieceRepository);

        final boolean result = PlaneVerifier.matches(plane, resultPlane);
        threadVizualizer.threadFinish(Thread.currentThread());
        threadVizualizer.close();

        System.out.println("[System]\t\tResult: planes " + (result ? "match" : "do not match"));
    }
}
