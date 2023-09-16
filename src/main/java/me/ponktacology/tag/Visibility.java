package me.ponktacology.tag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Visibility {

    public interface Strategy {
        boolean canSee(Player player, Player other);
    }

    public static void update(Player player, Player other, Strategy strategy) {
        if (strategy.canSee(player, other)) {
            player.showPlayer(other);
        } else {
            player.hidePlayer(other);
        }
    }

    public static void update(Player player, Strategy strategy) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            update(player, other, strategy);
            update(other, player, strategy);
        }
    }
}
