package me.ponktacology.tag.game;

public class Statistic {

    public enum Type {
        WIN("Won Games"),
        LOOSE("Lost Games"),
        KILL("Killed Players"),
        ASSIST("Assisted Players"),
        HIT("Tagged Players"),
        DAMAGED("Got Tagged");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private final Type type;
    private int value;
    private boolean dirty;

    public Statistic(Type type, int value) {
        this.type = type;
        this.value = value;
    }

    public void increment(int value) {
        this.value += value;
        this.dirty = true;
    }

    public int value() {
        return value;
    }

    public Type type() {
        return type;
    }

    public boolean dirty() {
        return dirty;
    }
}
