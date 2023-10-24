package me.ponktacology.tag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Constants {

    public static class Database {
        public static String DB_NAME = "customer_587131_tag";
        public static String DB_USERNAME = "customer_587131_tag";
        public static String DB_PASSWORD = "h9j77pu7WIWGZ3C7@$ln";
        public static String DB_HOST = "na03-sql.pebblehost.com";
        public static int DB_PORT = 3306;
    }

    public static long NEAREST_PLAYER_COMPASS_UPDATE_DELAY = 20; // In ticks
    public static long ASSIST_TIME = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    public static int MAX_PLAYERS = 5;
    public static int REQUIRED_PLAYERS = 2;
    public static int COUNTDOWN_DURATION = 5; //In seconds
    public static long ROUND_DURATION = TimeUnit.MILLISECONDS.convert(45, TimeUnit.SECONDS); // In milliseconds

    public static long FIRST_ROUND_DURATION = TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
    public static double INITIALLY_TAGGED_FACTOR = 0.25;
    public static Location HUB_SPAWN = new Location(Bukkit.getWorlds().get(0), 0, 5, 0);
    public static long COMBAT_LOG_TIME = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    public static long PARTY_INVITE_EVICTION_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    public static boolean CAN_SPECTATORS_SEE_OTHER_SPECTATORS = true;
    public static int DEFAULT_MAX_PARTY_SIZE = 5;

    public static int getMaxPartySize(Player player) {
        var maxSize = DEFAULT_MAX_PARTY_SIZE;
        for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
            for (Map.Entry<String, Boolean> permission : attachmentInfo.getAttachment().getPermissions().entrySet()) {
                if (permission.getValue() && permission.getKey().startsWith("party.size")) {
                    maxSize = Math.max(maxSize, Integer.parseInt(permission.getKey().split("party.size.")[1]));
                }
            }
        }
        return maxSize;
    }

}
