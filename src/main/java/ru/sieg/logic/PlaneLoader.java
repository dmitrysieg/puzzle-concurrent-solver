package ru.sieg.logic;

import ru.sieg.logic.domain.Plane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class PlaneLoader {

    public Plane load(final String imgName) {

        final BufferedImage img;

        try {
            final URL puzzleUrl = getClass().getClassLoader().getResource(imgName);
            if (puzzleUrl != null) {
                img = ImageIO.read(puzzleUrl);
            } else {
                throw new IOException("File not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        return new Plane(img.getWidth(), img.getHeight(), img);
    }
}
