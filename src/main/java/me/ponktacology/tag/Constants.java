package me.ponktacology.tag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constants {
    public static final long ASSIST_TIME = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);

    public static class Database {
        public static final String NAME = "tnt-tag";
        public static final String USERNAME = "user";
        public static final String PASSWORD = "1234";
        public static final String HOST = "localhost";
        public static final int PORT = 5432;
    }
    public static final int MAX_PLAYERS = 5;
    public static final int REQUIRED_PLAYERS = 2;
    public static final int COUNTDOWN_DURATION = 5; //In seconds
    public static final long ROUND_DURATION = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS); // In milliseconds

    public static final long FIRST_ROUND_DURATION = TimeUnit.MILLISECONDS.convert(45, TimeUnit.SECONDS);
    public static final float INITIALLY_TAGGED_FACTOR = 0.25f;
    public static final Location MAP_SPAWN = new Location(Bukkit.getWorlds().get(0), 5, 5, 5);
    public static final Location LOBBY_SPAWN = new Location(Bukkit.getWorlds().get(0), -5, 5, -5);

    public static final Location HUB_SPAWN = new Location(Bukkit.getWorlds().get(0), 0, 5, 0);
    public static final long COMBAT_LOG_TIME = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    public static final long PARTY_INVITE_EVICTION_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    public static final boolean CAN_SPECTATORS_SEE_OTHER_SPECTATORS = true;
    public static final int DEFAULT_MAX_PARTY_SIZE = 5;
    public static final Map<String, Integer> MAX_PARTY_SIZE_PER_PERMISSION = new HashMap<>();

    static {
        MAX_PARTY_SIZE_PER_PERMISSION.put("party.size.vip", 15);
        MAX_PARTY_SIZE_PER_PERMISSION.put("party.size.svip", 25);
    }

    public static int getMaxPartySize(Player player) {
        var maxSize = DEFAULT_MAX_PARTY_SIZE;
        for (Map.Entry<String, Integer> entry : MAX_PARTY_SIZE_PER_PERMISSION.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                maxSize = Math.max(entry.getValue(), maxSize);
            }
        }
        return maxSize;
    }

}
