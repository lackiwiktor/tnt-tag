package me.ponktacology.tag;

import me.ponktacology.tag.game.GameCommands;
import me.ponktacology.tag.game.GameTracker;
import me.ponktacology.tag.hotbar.Hotbar;
import me.ponktacology.tag.menu.Menu;
import me.ponktacology.tag.party.Party;
import me.ponktacology.tag.party.PartyArgumentProvider;
import me.ponktacology.tag.party.PartyCommands;
import me.ponktacology.tag.scoreboard.Scoreboard;
import me.ponktacology.tag.statistics.StatisticsCommands;
import me.ponktacology.tag.statistics.StatisticsTracker;
import me.vaperion.blade.Blade;
import me.vaperion.blade.bukkit.BladeBukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        GameTracker.INSTANCE.initialize();
        Hub.INSTANCE.initialize();
        StatisticsTracker.INSTANCE.initialize();
        Scoreboard.INSTANCE.initialize();
        Hotbar.initialize();
        Menu.initialize();
        Blade.forPlatform(new BladeBukkitPlatform(this)).bind(binder -> binder.bind(Party.class, new PartyArgumentProvider())).build()
                .register(GameCommands.class)
                .register(PartyCommands.class)
                .register(StatisticsCommands.class);
        Database.INSTANCE.update("CREATE TABLE IF NOT EXISTS statistics(id INT AUTO_INCREMENT PRIMARY KEY, uuid  VARCHAR(255), type  VARCHAR(255), value INT)", s -> {});
    }

    public static Plugin get() {
        return JavaPlugin.getPlugin(Plugin.class);
    }

}
