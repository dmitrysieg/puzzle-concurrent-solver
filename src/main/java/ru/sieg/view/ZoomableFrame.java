package ru.sieg.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class ZoomableFrame extends JFrame {

    @Override
    public void paint(Graphics g) {


        Graphics2D g2 = (Graphics2D) g;
        final int w = g2.getClipBounds().width;
        final int h = g2.getClipBounds().height;
        g2.setClip(0, 0, w * 2, h * 2);

        AffineTransform at = new AffineTransform();
        at.scale(0.5, 0.5);
        at.translate(w * 0.5, h * 0.5);
        g2.transform(at);

        super.paint(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.getWidth() * 2, this.getHeight() * 2);
    }
}
