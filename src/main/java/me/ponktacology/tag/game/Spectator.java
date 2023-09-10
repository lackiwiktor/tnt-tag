package me.ponktacology.tag.game;

import me.ponktacology.tag.Constants;
import me.ponktacology.tag.Hub;
import me.ponktacology.tag.PlayerUtil;
import me.ponktacology.tag.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Spectator {

    private final UUID uuid;

    public Spectator(UUID uuid) {
        this.uuid = uuid;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void setup(Visibility.Strategy visibilityStrategy) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline");
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        Visibility.update(player, visibilityStrategy);
        player.teleport(Constants.LOBBY_SPAWN);
        PlayerUtil.resetPlayer(player);
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
}