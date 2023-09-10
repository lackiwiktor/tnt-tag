package me.ponktacology.tag;

import me.ponktacology.tag.game.GameTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public enum Hub {
    INSTANCE;

    private static final Visibility.Strategy VISIBILITY_STRATEGY = (player, other) -> false;

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(new HubListener(), Plugin.get());
    }

    public void moveToHub(Player player) {
        prepareForHub(player);
        player.teleport(Constants.HUB_SPAWN);
    }

    private void prepareForHub(Player player) {
        Visibility.update(player, VISIBILITY_STRATEGY);
        PlayerUtil.resetPlayer(player);
    }

    public boolean isInHub(Player player) {
        return !GameTracker.INSTANCE.isInGame(player);
    }

    public static class HubListener implements Listener {

        @EventHandler
        public void on(PlayerSpawnLocationEvent player) {
            player.setSpawnLocation(Constants.HUB_SPAWN);
        }

        @EventHandler
        public void on(PlayerJoinEvent event) {
            Hub.INSTANCE.prepareForHub(event.getPlayer());
        }
    }
}
