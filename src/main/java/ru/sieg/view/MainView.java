package ru.sieg.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class MainView implements Runnable {

    private int DEFAULT_WIDTH = 640;
    private int DEFAULT_HEIGHT = 480;
    private JFrame jFrame;
    private BufferStrategy bufferStrategy;
    private long frameTime = 0;

    public static MainView create() {
        final MainView result = new MainView();
        SwingUtilities.invokeLater(result);
        return result;
    }

    private void setDefaultSize() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jFrame.setBounds(screenSize.width / 2 - DEFAULT_WIDTH / 2, screenSize.height / 2 - DEFAULT_HEIGHT / 2,
                DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void show(final BufferedImage img) {
        if (bufferStrategy == null) {
            return;
        }
        final Graphics g = bufferStrategy.getDrawGraphics();
        if (g == null) {
            return;
        }
        final long curTime = System.currentTimeMillis();
        if (curTime - frameTime < (1000 / 25)) {
            return;
        }
        frameTime = curTime;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        g.drawImage(img, DEFAULT_WIDTH / 2 - img.getWidth() / 2, DEFAULT_HEIGHT / 2 - img.getHeight() / 2, null);
        g.dispose();
        bufferStrategy.show();

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
    }
}
