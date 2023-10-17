package me.ponktacology.tag.hotbar;

import com.google.common.base.Preconditions;
import me.ponktacology.tag.Hub;
import me.ponktacology.tag.ItemBuilder;
import me.ponktacology.tag.Plugin;
import me.ponktacology.tag.game.GameTracker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public enum Hotbar {

    IN_GAME(new HotbarItem[]{new HotbarItem(new ItemBuilder(Material.COMPASS).name("&eNearest player"), event -> {
    })}),
    SPECTATOR(new HotbarItem[]{null, null, null, null, null, null, null, null, new HotbarItem(new ItemBuilder(Material.COMPASS).name("&eTeleporter"), event -> {
    }), new HotbarItem(new ItemBuilder(Material.COMPASS).name("&ePlay Again"), event -> {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final var game = GameTracker.INSTANCE.getByPlayer(event.getPlayer());
        if (game == null) throw new IllegalStateException("not in game");
        game.handleQuit(event.getPlayer());
        GameTracker.INSTANCE.joinQueue(event.getPlayer());
    }), new HotbarItem(new ItemBuilder(Material.COMPASS).name("&eReturn to Hub"), event -> {
        System.out.println("CANODAWOND");
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        System.out.println("DAWWAWD");
        final var game = GameTracker.INSTANCE.getByPlayer(event.getPlayer());
        if (game == null) throw new IllegalStateException("not in game");
        game.handleQuit(event.getPlayer());
        Hub.INSTANCE.moveToHub(event.getPlayer());
        System.out.println("DAWAW");
    })}),
    IN_LOBBY(new HotbarItem[]{null, null, null, null, null, null, null, null, new HotbarItem(new ItemBuilder(Material.BED).name("&cReturn to Hub").lore("&7Right-click to return to the hub"), event -> {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final var player = event.getPlayer();
        final var game = GameTracker.INSTANCE.getByPlayer(player);
        if (game == null) throw new IllegalStateException("not in game");
        game.handleQuit(player);
        Hub.INSTANCE.moveToHub(player);
    }),});

    private final HotbarItem[] items;

    Hotbar(HotbarItem[] items) {
        this.items = items;
    }

    private boolean click(PlayerInteractEvent event) {
        final var item = event.getItem();
        if (item == null) return false;
        for (HotbarItem hotbarItem : items) {
            if (hotbarItem == null) continue;
            if (hotbarItem.icon.isSimilar(item)) {
                hotbarItem.onClick(event);
                return true;
            }
        }
        return false;
    }

    public void apply(Player player) {
        player.getInventory().clear();
        for (int i = 0; i < items.length; i++) {
            final var item = items[i];
            player.getInventory().setItem(i, item == null ? null : item.icon);
        }
    }

    public static void initialize() {
        Bukkit.getPluginManager().registerEvents(new HotbarListener(), Plugin.get());
    }

    private static void handleClick(PlayerInteractEvent event) {
        for (Hotbar hotbar : values()) {
            if (hotbar.click(event)) return;
        }
    }

    private static class HotbarItem {

        private final ItemStack icon;
        private final Consumer<PlayerInteractEvent> consumer;

        private HotbarItem(ItemStack icon, Consumer<PlayerInteractEvent> consumer) {
            this.icon = icon;
            this.consumer = consumer;
        }

        private void onClick(PlayerInteractEvent event) {
            consumer.accept(event);
        }
    }

    private static class HotbarListener implements Listener {

        @EventHandler
        public void on(PlayerInteractEvent event) {
            handleClick(event);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(InventoryClickEvent event) {
            event.setCancelled(true);
        }
    }
}
