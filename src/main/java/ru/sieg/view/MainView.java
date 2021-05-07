package ru.sieg.view;

import ru.sieg.logic.PieceRepository;
import ru.sieg.logic.Solver;
import ru.sieg.logic.SolverCompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.Frame.MAXIMIZED_BOTH;

public class MainView implements Runnable {

    private JFrame jFrame;
    private BufferStrategy bufferStrategy;
    private long frameTime = 0;
    private BufferedImage img;

    private PieceRepository pieceRepository;
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
        this.pieceRepository = pieceRepository;

        final Container container = jFrame.getContentPane();
        container.add(new PieceRepositoryView(pieceRepository));
    }

    private void setDefaultSize() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setBounds(0, 0, screenSize.width, screenSize.height);
    }

    public void update(final Solver solver, final BufferedImage img, final String title) {
        this.solverViewMap.get(solver).updateClip();
        jFrame.setTitle(title);
    }

    public void show(final BufferedImage img, final String title) {
        this.show(img, title, false);
    }

    public void show(final BufferedImage img, final String title, boolean force) {

        this.img = img;
        if (bufferStrategy == null) {
            return;
        }
        final Graphics g = bufferStrategy.getDrawGraphics();
        if (g == null) {
            return;
        }
        final long curTime = System.currentTimeMillis();
        if (!force && curTime - frameTime < (1000 / 25)) {
            return;
        }
        frameTime = curTime;

        final Rectangle formSize = jFrame.getBounds();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, formSize.width, formSize.height);
        g.drawImage(img, formSize.width / 2 - img.getWidth() / 2, formSize.height / 2 - img.getHeight() / 2, null);
        g.dispose();
        bufferStrategy.show();

        jFrame.setTitle(title);
    }

    @Override
    public void run() {
        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setDefaultSize();
        jFrame.setExtendedState(MAXIMIZED_BOTH);
        jFrame.setVisible(true);

        final Container container = jFrame.getContentPane();
        final FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);
        container.setLayout(flowLayout);

        jFrame.setIgnoreRepaint(true);
        jFrame.createBufferStrategy(2);
        bufferStrategy = jFrame.getBufferStrategy();

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
