package ru.sieg.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class SolverLogo {

    private BufferedImage img;

    public SolverLogo(final BufferedImage img) {
        this.img = img;
    }

    public BufferedImage getImg() {
        return img;
    }

    public static SolverLogo create(final int size, final String id) {

        final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        final Random random = new Random(id.hashCode());

        g.setColor(Color.ORANGE);
        final int center = (size - 1) / 2;
        for (int i = 0; i < size; i++) {
            final int width = random.nextInt(center);
            g.drawLine(center - width, i,center + width, i);
        }
        return new SolverLogo(img);
    }
}
