package me.ponktacology.tag.map;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class Arena {

    private final String id;
    private @Nullable String displayName;
    private @Nullable Location lobbySpawn;
    private @Nullable Location gameSpawn;

    public Arena(String id) {
        this.id = id;
    }

    public Arena(String id, @Nullable String displayName, @Nullable Location lobbySpawn, @Nullable Location gameSpawn) {
        this.id = id;
        this.displayName = displayName;
        this.lobbySpawn = lobbySpawn;
        this.gameSpawn = gameSpawn;
    }

    public boolean isSetup() {
        return lobbySpawn != null && gameSpawn != null;
    }

    public String getId() {
        return id;
    }

    public @Nullable Location getLobbySpawn() {
        return lobbySpawn;
    }

    public @Nullable Location getGameSpawn() {
        return gameSpawn;
    }

    public @Nullable String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void setGameSpawn(Location gameSpawn) {
        this.gameSpawn = gameSpawn;
    }
}
