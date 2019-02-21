package ru.sieg.logic.domain;

import java.util.Random;

public class Plane {

    private final int horizontalSize;
    private final int verticalSize;
    private final int[][] values;

    public Plane(final int horizontalSize, final int verticalSize) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;

        values = new int[verticalSize][horizontalSize];
    }

    public int getHorizontalSize() {
        return horizontalSize;
    }

    public int getVerticalSize() {
        return verticalSize;
    }

    public int[][] getValues() {
        return values;
    }

    public int getValue(final int x, final int y) {
        return values[y][x];
    }

    public Plane initValues() {
        final Random random = new Random(Long.MAX_VALUE - System.currentTimeMillis());
        for (int y = 0; y < verticalSize; y++) {
            for (int x = 0; x < horizontalSize; x++) {
                values[y][x] = random.nextInt();
            }
        }
        return this;
    }
}
