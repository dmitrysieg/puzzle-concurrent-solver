package ru.sieg.logic.utils;

import ru.sieg.logic.domain.Piece;
import ru.sieg.logic.domain.Profile;
import ru.sieg.logic.domain.Side;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Non-concurrent so far. todo
 */
public class IndexMap {

    private Map<Side, TreeMap<Profile, List<Piece>>> side_index;

    /**
     * Added to speed up operations irrelative to particular sides.
     */
    private TreeMap<Profile, List<Piece>> ref_index;

    /**
     * Added to speep up size(), isEmpty() and akin operations.
     */
    private AtomicInteger size = new AtomicInteger();

    public IndexMap() {
        side_index = new HashMap<>();
        for (final Side side : Side.values()) {
            side_index.put(side, new TreeMap<>());
        }
        ref_index = side_index.get(Side.NORTH);
    }

    public IndexMap(final Collection<Piece> pieces) {
        super();
        pieces.forEach(this::add);
    }

    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size.get() <= 0;
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
        size.incrementAndGet();
    }

    public Stream<Piece> stream() {
        return ref_index.values().stream().flatMap(Collection::stream);
    }

    public Piece getFirst() {
        final Iterator<List<Piece>> it = ref_index.values().iterator();
        if (it.hasNext()) {
            return it.next().get(0);
        }
        return null;
    }

    private int indexSize(final Side side) {
        return (int) side_index.get(side).values().stream().mapToLong(Collection::size).sum();
    }

    public void remove(final Piece piece) {
        for (final Side side : Side.values()) {

            final TreeMap<Profile, List<Piece>> index = side_index.get(side);
            final Profile profile = piece.getProfile(side);
            final List<Piece> bucket = index.get(profile);
            bucket.remove(piece);
            if (bucket.isEmpty()) {
                index.remove(profile);
            }
        }
        size.decrementAndGet();
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
