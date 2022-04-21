package ru.sieg.view;

import ru.sieg.logic.PieceRepository;
import ru.sieg.logic.Solver;
import ru.sieg.logic.SolverCompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.Frame.MAXIMIZED_BOTH;

public class MainView implements Runnable {

    private JFrame jFrame;

    private SolverCompany solverCompany;
    private Map<Solver, SolverView> solverViewMap = new ConcurrentHashMap<>();

    public static MainView create() {
        final MainView result = new MainView();
        SwingUtilities.invokeLater(result);
        return result;
    }

    public void put(SolverCompany solverCompany) {
        this.solverCompany = solverCompany;

        final Container container = jFrame.getContentPane();

        solverCompany.getSolvers().forEach(s -> {
            final SolverView solverView = new SolverView(s);
            solverViewMap.put(s, solverView);
            container.add(solverView);
        });
    }

    public void put(PieceRepository pieceRepository) {
        final Container container = jFrame.getContentPane();
        container.add(new PieceRepositoryView(pieceRepository));
    }

    private void setDefaultSize() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setBounds(0, 0, screenSize.width, screenSize.height);
    }

    public void update(final Solver solver) {
        this.solverViewMap.get(solver).updateClip();
    }

    @Override
    public void run() {
        jFrame = new ZoomableFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setDefaultSize();
        jFrame.setExtendedState(MAXIMIZED_BOTH);
        jFrame.setVisible(true);

        final Container container = jFrame.getContentPane();
        final CircularLayout circularLayout = new CircularLayout(10);
        container.setLayout(circularLayout);

        jFrame.setIgnoreRepaint(true);
        jFrame.createBufferStrategy(2);

        jFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    solverCompany.togglePause();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        final Timer timer = new Timer(25, e -> {
            jFrame.getContentPane().doLayout();
            jFrame.repaint();
        });
        timer.start();
    }
}
