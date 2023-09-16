package me.ponktacology.tag;

import me.ponktacology.tag.game.GameCommands;
import me.ponktacology.tag.game.GameTracker;
import me.ponktacology.tag.party.Party;
import me.ponktacology.tag.party.PartyArgumentProvider;
import me.ponktacology.tag.party.PartyCommands;
import me.ponktacology.tag.scoreboard.Scoreboard;
import me.ponktacology.tag.statistics.StatisticsCommands;
import me.ponktacology.tag.statistics.StatisticsTracker;
import me.vaperion.blade.Blade;
import me.vaperion.blade.bukkit.BladeBukkitPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        GameTracker.INSTANCE.initialize();
        Hub.INSTANCE.initialize();
        StatisticsTracker.INSTANCE.initialize();
        Scoreboard.INSTANCE.initialize();
        Blade.forPlatform(new BladeBukkitPlatform(this)).bind(binder -> binder.bind(Party.class, new PartyArgumentProvider())).build()
                .register(GameCommands.class)
                .register(PartyCommands.class)
                .register(StatisticsCommands.class);
        Database.INSTANCE.update("CREATE TABLE IF NOT EXISTS statistics (key SERIAL NOT NULL PRIMARY KEY, id VARCHAR NOT NULL, type VARCHAR NOT NULL, value INTEGER NOT NULL)", statement -> {
        });
    }


    public static Plugin get() {
        return JavaPlugin.getPlugin(Plugin.class);
    }

}
