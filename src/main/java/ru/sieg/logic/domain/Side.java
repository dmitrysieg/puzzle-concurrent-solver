package ru.sieg.logic.domain;

public enum Side {

    NORTH, EAST, SOUTH, WEST;

    public Side getComplimentary() {
        final int index = (this.ordinal() + 2) % 4;
        return Side.values()[index];
    }
}
