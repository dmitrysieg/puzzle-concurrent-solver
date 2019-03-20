package ru.sieg.logic.domain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Plane {

    private final int horizontalSize;
    private final int verticalSize;
    private final BufferedImage img;

    public Plane(final int horizontalSize, final int verticalSize) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;

        try {
            final URL puzzleUrl = getClass().getClassLoader().getResource("puzzle.jpg");
            if (puzzleUrl != null) {
                img = ImageIO.read(puzzleUrl);
            } else {
                throw new IOException("File not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public int getHorizontalSize() {
        return horizontalSize;
    }

    public int getVerticalSize() {
        return verticalSize;
    }

    public Color getValue(final int x, final int y) {
        return new Color(img.getRGB(x, y));
    }

    public Plane initValues() {
        return this;
    }
}
