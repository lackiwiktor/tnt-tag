package me.ponktacology.tag.game;

import me.ponktacology.tag.Constants;
import me.ponktacology.tag.Hub;
import me.ponktacology.tag.PlayerUtil;
import me.ponktacology.tag.Visibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Participant {

    private final UUID uuid;
    private boolean tagged;
    private long lastUnTag;
    private Map<Participant, Long> combatHistory = new HashMap<>();

    public Participant(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void applyEffects() {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline effects");
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
    }

    public boolean isTagged() {
        return tagged;
    }

    public void markAsTagged(Participant taggedBy) {
        combatHistory.put(taggedBy, System.currentTimeMillis());
        markAsTagged();
    }

    public void markAsTagged() {
        this.tagged = true;
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline tag");
        player.sendMessage("You are IT.");
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
        System.out.println(player.getActivePotionEffects());
    }

    public void markAsNotTagged() {
        this.tagged = false;
        this.lastUnTag = System.currentTimeMillis();
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline un-tag");
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.removePotionEffect(PotionEffectType.SPEED);
        applyEffects();
    }


    public @Nullable Participant getTaggedBy() {
        final var entry = combatHistory.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if (entry == null) return null;
        return entry.getKey();
    }

    public boolean hasTaggedRecently() {
        return System.currentTimeMillis() - lastUnTag < Constants.COMBAT_LOG_TIME;
    }

    public void teleport(Location location) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline teleport");
        player.teleport(location);
    }

    public void sendMessage(String message) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline message");
        player.sendMessage(message);
    }

    public String getName() {
        final var player = getPlayer();
        if (player == null) return "Unknown";
        return player.getName();
    }

    private @Nullable Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Spectator toSpectator() {
        return new Spectator(uuid);
    }

    public void updateVisibility(Visibility.Strategy strategy) {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline visibility update");
        Visibility.update(player, strategy);
    }

    public void reset() {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline reset");
        PlayerUtil.resetPlayer(player);
    }

    public void moveToHub() {
        final var player = getPlayer();
        if (player == null) throw new IllegalStateException("offline move to hub");
        Hub.INSTANCE.moveToHub(player);
    }
}
