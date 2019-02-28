package ru.sieg.logic;

import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;
import ru.sieg.logic.utils.IndexMap;

import java.util.*;

public class PieceRepository {

    private final IndexMap pieces;
    private final Random random;

    public PieceRepository() {
        pieces = new IndexMap();
        random = new Random(System.currentTimeMillis());
    }

    public int size() {
        return pieces.size();
    }

    public boolean isEmpty() {
        return pieces.isEmpty();
    }

    public void add(final Piece piece) {
        pieces.add(piece);
    }

    public void shuffle() {
//        Collections.shuffle(pieces, new Random());
    }

    public Optional<Piece> popRandomPiece() {
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        if (pieces.size() == 1) {
            final Piece piece = pieces.stream().findFirst().get();
            pieces.remove(piece);
            return Optional.of(piece);
        }
        final int index = random.nextInt(pieces.size());
        final Piece piece = pieces.stream().skip(index).findFirst().get();
        pieces.remove(piece);
        return Optional.of(piece);
    }

    public Piece pop(final Piece piece) {
        pieces.remove(piece);
        return piece;
    }

    public Optional<Piece> findPiece(final Side searchedSide, final Profile searchedProfile) {

        long ms = System.currentTimeMillis();

        final Optional<Piece> result = pieces.searchByProfile(searchedProfile, searchedSide);

//        System.out.println("findPiece(): " + (System.currentTimeMillis() - ms) + "ms ");

        return result;
    }
}
