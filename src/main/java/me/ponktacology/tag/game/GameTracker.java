package me.ponktacology.tag.game;

import me.ponktacology.tag.Plugin;
import me.ponktacology.tag.map.ArenaTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public enum GameTracker {
    INSTANCE;

    private final Set<Game> games = new HashSet<>();

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(new GameListener(), Plugin.get());
    }

    public Game start(boolean privateGame) {
        final var arena = ArenaTracker.INSTANCE.getRandom();
        final var game = new Game(arena, privateGame, games::remove);
        game.start();
        games.add(game);
        return game;
    }

    public void joinQueue(Player player) {
        final var currentGame = getByPlayer(player);
        if (currentGame != null) currentGame.handleQuit(player);

        for (Game game : games) {
            if (!game.isPrivate() && game.join(player)) {
                return;
            }
        }

        start(false);
        joinQueue(player); //Call recursively
    }

    public boolean isInGame(Player player) {
        return getByPlayer(player) != null;
    }

    public @Nullable Game getByPlayer(Player player) {
        for (Game game : games) {
            if (game.isInGame(player)) return game;
        }
        return null;
    }

    public Set<Game> games() {
        return games;
    }

    private static class GameListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(EntityDamageByEntityEvent event) {
            final var damager = event.getDamager();
            final var victim = event.getEntity();
            if (!(damager instanceof Player && victim instanceof Player)) return;
            for (Game game : GameTracker.INSTANCE.games()) {
                if (game.handleCombat(event)) return; // We return when first game handles the combat
            }
        }

        @EventHandler
        public void on(PlayerQuitEvent event) {
            for (Game game : GameTracker.INSTANCE.games()) {
                if (game.handleQuit(event.getPlayer())) return;
            }
        }

        @EventHandler
        public void on(EntityDamageEvent event) {
            final var victim = event.getEntity();
            if (!(victim instanceof Player)) return;
            for (Game game : GameTracker.INSTANCE.games()) {
                if (game.handleDamage(event)) return;
            }
        }
    }
}
