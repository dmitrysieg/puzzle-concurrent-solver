package ru.sieg.logic.utils;

import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;

import java.util.*;
import java.util.stream.Stream;

/**
 * Non-concurrent so far. todo
 */
public class IndexMap {

    private Map<Side, TreeMap<Profile, List<Piece>>> side_index;

    public IndexMap() {
        side_index = new HashMap<>();
        for (final Side side : Side.values()) {
            side_index.put(side, new TreeMap<>());
        }
    }

    public IndexMap(final Collection<Piece> pieces) {
        super();
        pieces.forEach(this::add);
    }

    public int size() {
        return (int) stream().count();
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    public void add(final Piece piece) {
        for (final Side side : Side.values()) {

            final TreeMap<Profile, List<Piece>> index = side_index.get(side);
            final Profile profile = piece.getProfile(side);

            if (!index.containsKey(profile)) {
                index.put(profile, new LinkedList<>());
            }
            side_index.get(side).get(profile).add(piece);
        }
    }

    public Stream<Piece> stream() {
        return side_index.get(Side.NORTH).values().stream().flatMap(Collection::stream);
    }

    private int indexSize(final Side side) {
        return (int) side_index.get(side).values().stream().flatMap(Collection::stream).count();
    }

    public void remove(final Piece piece) {
        for (final Side side : Side.values()) {

            final TreeMap<Profile, List<Piece>> index = side_index.get(side);
            final Profile profile = piece.getProfile(side);
            final List<Piece> bucket = index.get(profile);
            bucket.remove(piece);
//            System.out.print(indexSize(side) + " ");
        }
//        System.out.println();
    }

    public Optional<Piece> searchByProfile(final Profile profile, final Side side) {

        final TreeMap<Profile, List<Piece>> index = side_index.get(side);
        final List<Piece> bucket = index.get(profile);

        if (bucket == null || bucket.isEmpty()) {
            return Optional.empty();
        }
        final Piece result = bucket.get(0);
        bucket.remove(0);
        return Optional.of(result);
    }
}
