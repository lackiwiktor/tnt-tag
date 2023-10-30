package me.ponktacology.tag.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Arena {

    private final String id;
    private String displayName;
    private Location lobbySpawn;
    private Location gameSpawn;

    public Arena(String id) {
        this.id = id;
        this.displayName = id;
        this.lobbySpawn = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        this.gameSpawn = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }

    public Arena(String id, String displayName, Location lobbySpawn, Location gameSpawn) {
        this.id = id;
        this.displayName = displayName;
        this.lobbySpawn = lobbySpawn;
        this.gameSpawn = gameSpawn;
    }

    public String getId() {
        return id;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public Location getGameSpawn() {
        return gameSpawn;
    }

    public String getDisplayName() {
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
