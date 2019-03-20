package ru.sieg.view;

import ru.sieg.logic.SolverCompany;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class MainView implements Runnable {

    private int DEFAULT_WIDTH = 640;
    private int DEFAULT_HEIGHT = 480;
    private JFrame jFrame;
    private BufferStrategy bufferStrategy;
    private long frameTime = 0;
    private BufferedImage img;

    private SolverCompany solverCompany;

    public static MainView create() {
        final MainView result = new MainView();
        SwingUtilities.invokeLater(result);
        return result;
    }

    public void setSolverCompany(SolverCompany solverCompany) {
        this.solverCompany = solverCompany;
    }

    private void setDefaultSize() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setBounds(screenSize.width / 2 - DEFAULT_WIDTH / 2, screenSize.height / 2 - DEFAULT_HEIGHT / 2,
                DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
        jFrame.setVisible(true);

        jFrame.setIgnoreRepaint(true);
        jFrame.createBufferStrategy(2);
        bufferStrategy = jFrame.getBufferStrategy();

        jFrame.getContentPane().addComponentListener(new ComponentListener() {

            private void redraw() {
                if (img != null) {
                    show(img, "", true);
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {redraw();}
            @Override public void componentMoved(ComponentEvent e) {}
            @Override public void componentShown(ComponentEvent e) {redraw();}
            @Override public void componentHidden(ComponentEvent e) {}
        });

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
    }
}
