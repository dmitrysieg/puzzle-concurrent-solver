package ru.sieg.logic;

import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;

import java.util.*;

public class PieceRepository {

    private final List<Piece> pieces;
    private final Random random;

    public PieceRepository() {
        pieces = new ArrayList<>();
        random = new Random(System.currentTimeMillis());
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void add(final Piece piece) {
        pieces.add(piece);
    }

    public void shuffle() {
        Collections.shuffle(pieces, new Random());
    }

    public Optional<Piece> popRandomPiece() {
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        final int index = random.nextInt(pieces.size());
        final Piece piece = pieces.get(index);
        pieces.remove(index);
        return Optional.of(piece);
    }

    public Piece pop(final PiecePlace piecePlace) {
        pieces.remove(piecePlace.index);
        return piecePlace.piece;
    }

    public Optional<PiecePlace> findPiece(final Side searchedSide, final Profile searchedProfile) {

        long ms = System.currentTimeMillis();

        for (int i = 0; i < pieces.size(); i++) {
            final Piece p = pieces.get(i);
            if (p.getProfile(searchedSide).compareTo(searchedProfile) == 0) {
                return Optional.of(new PiecePlace(p, i));
            }
        }

        System.out.println("findPiece(): " + (System.currentTimeMillis() - ms) + "ms ");

        return Optional.empty();
    }

    public static class PiecePlace {

        private final Piece piece;
        private final int index;

        public PiecePlace(final Piece piece, final int index) {
            this.piece = piece;
            this.index = index;
        }

        public Piece getPiece() {
            return piece;
        }

        public int getIndex() {
            return index;
        }
    }
}
