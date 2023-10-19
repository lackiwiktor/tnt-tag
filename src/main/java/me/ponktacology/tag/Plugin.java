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
        Database.INSTANCE.update("CREATE TABLE IF NOT EXISTS statistics (key SERIAL NOT NULL PRIMARY KEY, id VARCHAR NOT NULL, type VARCHAR NOT NULL, value INTEGER NOT NULL)", s -> {
        });

        for (Field field : Constants.class.getFields()) {
            if (getConfig().contains(field.getName())) {
                try {
                    field.set(null, getConfig().get(field.getName()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    getConfig().set(field.getName(), field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                saveConfig();
            }
        }
    }

    public static Plugin get() {
        return JavaPlugin.getPlugin(Plugin.class);
    }

}
