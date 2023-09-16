package me.ponktacology.tag.statistics;

import me.ponktacology.tag.Plugin;
import me.ponktacology.tag.Scheduler;
import me.ponktacology.tag.game.Statistic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum StatisticsTracker {
    INSTANCE;

    private final Map<UUID, Statistics> statistics = new ConcurrentHashMap<>();

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(new StatisticsListener(), Plugin.get());
    }

    public int get(Player player, Statistic.Type type) {
        return statistics.computeIfAbsent(player.getUniqueId(), Statistics::new).get(type);
    }

    public void incrementByOne(UUID uuid, Statistic.Type type) {
        statistics.computeIfAbsent(uuid, Statistics::new).increment(type, 1);
    }

    private void load(UUID player) {
        final var statistics = new Statistics(player);
        this.statistics.put(player, statistics);
        System.out.println("PUT");
        statistics.fetch();
    }

    private void saveAndFlush(Player player) {
        final var statistics = this.statistics.remove(player.getUniqueId());
        if (statistics == null) return;
        statistics.save();
    }

    public void print(Player actor, Player target) {
        final var statistics = this.statistics.computeIfAbsent(target.getUniqueId(), Statistics::new);
        for (Statistic.Type type : Statistic.Type.values()) {
            actor.sendMessage(type.displayName() + ": " + statistics.get(type));
        }
    }

    private static class StatisticsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void on(AsyncPlayerPreLoginEvent event) {
            if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
            StatisticsTracker.INSTANCE.load(event.getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void on(PlayerQuitEvent event) {
            Scheduler.async(() -> StatisticsTracker.INSTANCE.saveAndFlush(event.getPlayer()));
        }
    }
}
