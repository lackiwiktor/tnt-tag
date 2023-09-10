package me.ponktacology.tag.game;

public class Statistic {

    public enum Type {
        WIN,
        LOOSE,
        KILL,
        ASSIST,
        HIT,
        DAMAGED;
    }

    private final Type type;
    private int value;

    public Statistic(Type type, int value) {
        this.type = type;
        this.value = value;
    }
}
