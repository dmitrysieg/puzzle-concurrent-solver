package ru.sieg.logic;

import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Plane;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;

import java.util.Arrays;
import java.util.Random;

import static ru.sieg.logic.domain.Profile.PROFILE_FLAT;

public class PlaneTearer {

    public static PieceRepository tear(final Plane plane) {

        long time = System.currentTimeMillis();

        final PieceRepository pieceRepository = new PieceRepository();
        // prev and curr lines
        final Piece[][] cachedPieces = new Piece[2][plane.getHorizontalSize()];

        final Random random = new Random(Long.MAX_VALUE - System.currentTimeMillis());
        for (int y = 0; y < plane.getVerticalSize(); y++) {
            for (int x = 0; x < plane.getHorizontalSize(); x++) {

                final Piece piece = new Piece(plane.getValue(x, y).getRGB());
                cachedPieces[0][x] = piece;

                if (y == 0) {
                    piece.setProfile(Side.NORTH, PROFILE_FLAT);
                } else {
                    final Piece prev = cachedPieces[1][x];
                    piece.setProfile(Side.NORTH, prev.getProfile(Side.SOUTH).getComplementaryProfile());
                }

                if (y == plane.getVerticalSize() - 1) {
                    piece.setProfile(Side.SOUTH, PROFILE_FLAT);
                } else {
                    piece.setProfile(Side.SOUTH, Profile.getRandomProfile(random));
                }

                if (x == 0) {
                    piece.setProfile(Side.WEST, PROFILE_FLAT);
                } else {
                    final Piece prev = cachedPieces[0][x - 1];
                    piece.setProfile(Side.WEST, prev.getProfile(Side.EAST).getComplementaryProfile());
                }

                if (x == plane.getHorizontalSize() - 1) {
                    piece.setProfile(Side.EAST, PROFILE_FLAT);
                } else {
                    piece.setProfile(Side.EAST, Profile.getRandomProfile(random));
                }

                cachedPieces[0][x] = piece;

                pieceRepository.add(piece);
            }

            cachedPieces[1] = Arrays.copyOf(cachedPieces[0], cachedPieces[1].length);
        }

        pieceRepository.shuffle();

        System.out.println("[Tearer]\t\ttear(): " + (System.currentTimeMillis() - time) + " ms");
        return pieceRepository;
    }
}
