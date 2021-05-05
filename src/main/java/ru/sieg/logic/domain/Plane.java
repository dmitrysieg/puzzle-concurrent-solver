package ru.sieg.logic.domain;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Plane {

    private final int horizontalSize;
    private final int verticalSize;
    private final BufferedImage img;

    public Plane(final int horizontalSize, final int verticalSize, final BufferedImage img) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
        this.img = img;
    }

    public int getHorizontalSize() {
        return horizontalSize;
    }

    public int getVerticalSize() {
        return verticalSize;
    }

    public int getPiecesAmount() {
        return horizontalSize * verticalSize;
    }

    public Color getValue(final int x, final int y) {
        return new Color(img.getRGB(x, y));
    }
}
