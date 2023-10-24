package me.ponktacology.tag.game;

import me.ponktacology.tag.Constants;
import me.ponktacology.tag.Hub;
import me.ponktacology.tag.PlayerUtil;
import me.ponktacology.tag.Visibility;
import me.ponktacology.tag.hotbar.Hotbar;
import me.ponktacology.tag.map.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class Spectator {

    private final UUID uuid;

    public Spectator(UUID uuid) {
        this.uuid = uuid;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void setup(Arena arena, Visibility.Strategy visibilityStrategy) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline");
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        Visibility.update(player, visibilityStrategy);
        player.teleport(arena.getLobbySpawn());
        PlayerUtil.resetPlayer(player);
        Hotbar.SPECTATOR.apply(player);
    }

    public void moveToHub() {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline move to hub");
        Hub.INSTANCE.moveToHub(player);
    }

    public void sendMessage(String message) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline message");
        player.sendMessage(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spectator spectator = (Spectator) o;
        return Objects.equals(uuid, spectator.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}