package ru.sieg.logic.domain;

import java.util.Objects;
import java.util.Random;

public class Profile implements Comparable<Profile> {

    private static final int PROFILE_LENGTH = 8;
    public static final Profile PROFILE_FLAT;

    static {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PROFILE_LENGTH; i++) {
            sb.append("0");
        }
        PROFILE_FLAT = new Profile(sb.toString());
    }

    private final String profile;

    public Profile(final String profile) {
        this.profile = profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile1 = (Profile) o;
        return Objects.equals(profile, profile1.profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile);
    }

    @Override
    public String toString() {
        return profile;
    }

    public Profile getComplementaryProfile() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.profile.length(); i++) {
            final char src = this.profile.charAt(i);
            final char baseChar = (src >= 'a' && src <= 'z') ? 'a' : 'A';
            final char endChar = baseChar == 'a' ? 'z' : 'Z';
            final char cmpChar = (char) (baseChar + (endChar - src));
            sb.append(cmpChar);
        }
        return new Profile(sb.toString());
    }

    public static Profile getRandomProfile(final Random random) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PROFILE_LENGTH; i++) {
            final int index = random.nextInt(26 * 2);
            final char charCase = index / 26 == 0 ? 'a' : 'A';
            final char nextChar = (char) (charCase + (index % 26));
            sb.append(nextChar);
        }
        return new Profile(sb.toString());
    }

    @Override
    public int compareTo(final Profile o) {
        if (o == null || this.profile.length() != o.profile.length()) {
            throw new IllegalStateException("Can't compare profiles " + this + " and " + o);
        }
        return this.profile.compareTo(o.profile);
    }

    public int complimentaryCompareTo(final Profile o) {
        if (o == null || this.profile.length() != o.profile.length()) {
            throw new IllegalStateException("Can't compare profiles " + this + " and " + o);
        }
        for (int i = 0; i < profile.length(); i++) {
            final int index1 = getIndex(profile.charAt(i));
            final int index2 = getIndex(o.profile.charAt(i));
            if (index1 + index2 != 25) {
                return index1 + index2 - 25;
            }
        }
        return 0;
    }

    private static int getIndex(final char c) {
        return (c >= 'a' && c <= 'z') ? ('z' - c) : ('Z' - c);
    }
}
