package me.ponktacology.tag;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerUtil {

    public static void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getOpenInventory().close();
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFireTicks(1);
        player.setLevel(0);
        player.setExhaustion(0);
        player.setFallDistance(0);
        player.setExhaustion(0);
        player.setSaturation(12);
        player.getActivePotionEffects().forEach(it -> player.removePotionEffect(it.getType()));
    }
}
