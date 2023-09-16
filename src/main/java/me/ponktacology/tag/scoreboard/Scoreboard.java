package me.ponktacology.tag.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import me.ponktacology.tag.Hub;
import me.ponktacology.tag.Plugin;
import me.ponktacology.tag.game.GameTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public enum Scoreboard {
    INSTANCE;

    private final Map<UUID, FastBoard> scoreboards = new HashMap<>();

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), Plugin.get());
        Bukkit.getScheduler().runTaskTimer(Plugin.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 2L, 2L);
    }

    private void update(Player player) {
        final var scoreboard = scoreboards.get(player.getUniqueId());
        if (scoreboard == null) return;
        scoreboard.updateLines(getScoreboard(player));
    }

    private void create(Player player) {
        final var scoreboard = new FastBoard(player);
        scoreboard.updateTitle("TAG");
        update(player);
    }

    private void flush(Player player) {
        final var scoreboard = scoreboards.remove(player.getUniqueId());
        if (scoreboard != null) scoreboard.delete();
    }

    public List<String> getScoreboard(Player player) {
        if (Hub.INSTANCE.isInHub(player)) return Collections.emptyList();
        final var game = GameTracker.INSTANCE.getByPlayer(player);
        if (game == null) return Collections.emptyList();
        return game.scoreboard(player);
    }

    private static class ScoreboardListener implements Listener {

        @EventHandler
        public void on(PlayerJoinEvent event) {
            final var player = event.getPlayer();
            Scoreboard.INSTANCE.create(player);
        }

        @EventHandler
        public void on(PlayerQuitEvent event) {
            final var player = event.getPlayer();
            Scoreboard.INSTANCE.flush(player);
        }
    }
}
