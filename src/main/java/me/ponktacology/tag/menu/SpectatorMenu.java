package me.ponktacology.tag.menu;

import me.ponktacology.tag.ItemBuilder;
import me.ponktacology.tag.Scheduler;
import me.ponktacology.tag.game.Game;
import me.ponktacology.tag.game.Spectator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SpectatorMenu extends Menu {

    private final Game game;

    public SpectatorMenu(Game game) {
        this.game = game;
    }

    @Override
    public String getTitle(Player player) {
        return "Spectate";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final var buttons = new HashMap<Integer, Button>();
        for (Spectator spectator : game.spectators()) {
            buttons.put(buttons.size(), new SpectatorButton(spectator));
        }
        return buttons;
    }

    private class SpectatorButton extends Button {

        private final Spectator spectator;

        private SpectatorButton(Spectator spectator) {
            this.spectator = spectator;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            final var spectatorPlayer = spectator.getPlayer();
            if (spectatorPlayer == null) throw new IllegalStateException("offline spectator");
            return new ItemBuilder(Material.SKULL_ITEM)
                    .name(spectatorPlayer.getName())
                    .owner(spectatorPlayer.getName())
                    .lore("Click to teleport.");
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (!clickType.isLeftClick()) return;
            if (game.isFinished()) {
                player.sendMessage("Game has finished.");
                player.closeInventory();
                return;
            }
            final var spectatorPlayer = spectator.getPlayer();
            if (spectatorPlayer == null || !game.isInGame(spectatorPlayer)) {
                player.sendMessage("Player has left the game.");
                Scheduler.later(() -> new SpectatorMenu(game).openMenu(player), 1);
                return;
            }

            player.teleport(spectatorPlayer.getLocation());
        }
    }
}
