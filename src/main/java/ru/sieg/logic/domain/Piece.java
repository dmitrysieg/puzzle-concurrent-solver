package ru.sieg.logic.domain;

import java.util.HashMap;
import java.util.Map;

public class Piece implements Comparable<Piece> {

    private final Map<Side, Profile> profiles;
    private final int value;

    public Piece(final int value) {
        this.profiles = new HashMap<>();
        this.value = value;
    }

    public Map<Side, Profile> getProfiles() {
        return profiles;
    }

    public void setProfile(final Side side, final Profile profile) {
        profiles.put(side, profile);
    }

    public Profile getProfile(final Side side) {
        return profiles.get(side);
    }

    public boolean isEdge(final Side side) {
        return Profile.PROFILE_FLAT.equals(getProfile(side));
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Piece{" + profiles.get(Side.NORTH) + ", " +
                profiles.get(Side.EAST) + ", " +
                profiles.get(Side.SOUTH) + ", " +
                profiles.get(Side.WEST) + " -> " +
                value +
                '}';
    }

    @Override
    public int compareTo(Piece o) {
        if (o == null) {
            return 1;
        }
        return (int) Math.signum(
                1 * Math.signum(this.value - o.value) +
                2 * this.getProfile(Side.NORTH).compareTo(o.getProfile(Side.NORTH)) +
                4 * this.getProfile(Side.EAST).compareTo(o.getProfile(Side.EAST)) +
                8 * this.getProfile(Side.SOUTH).compareTo(o.getProfile(Side.SOUTH)) +
                16 * this.getProfile(Side.WEST).compareTo(o.getProfile(Side.WEST))
        );
    }
}
