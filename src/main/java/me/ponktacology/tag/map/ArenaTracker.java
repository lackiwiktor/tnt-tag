package me.ponktacology.tag.map;

import me.ponktacology.tag.Plugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public enum ArenaTracker {
    INSTANCE;

    private final File file;
    private final FileConfiguration configuration;
    private final Map<String, Arena> arenas = new HashMap<>();

    ArenaTracker() {
        file = new File(Plugin.get().getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public @Nullable Arena getById(String id) {
        return arenas.get(id.toUpperCase(Locale.ROOT));
    }

    public void create(String id) {
        arenas.put(id.toUpperCase(Locale.ROOT), new Arena(id));
    }

    private void load() {
        final var arenas = configuration.getConfigurationSection("arenas");
        if (arenas == null) {
            System.out.println("Loaded 0 arenas.");
            return;
        }

        for (String id : arenas.getKeys(false)) {
            final var displayName = arenas.getString(id + ".displayName");
            final var lobbyLocation = (Location) arenas.get(id + ".lobbySpawn");
            final var gameLocation = (Location) arenas.get(id + ".gameSpawn");
            this.arenas.put(id.toUpperCase(Locale.ROOT), new Arena(id, displayName, lobbyLocation, gameLocation));
        }

        System.out.println("Loaded " + this.arenas.size() + " arenas.");
    }

    private void save() {
        for (Arena arena : arenas.values()) {
            configuration.set("arenas." + arena.getId() + ".displayName", arena.getDisplayName());
            configuration.set("arenas." + arena.getId() + ".lobbySpawn", arena.getLobbySpawn());
            configuration.set("arenas." + arena.getId() + ".gameSpawn", arena.getGameSpawn());
        }

        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Arena getRandom() {
        final var readyArenas = arenas.values().stream().filter(Arena::isSetup).toArray(Arena[]::new);
        if (readyArenas.length == 0) throw new UnsupportedOperationException("no arenas");
        return readyArenas[ThreadLocalRandom.current().nextInt(readyArenas.length)];
    }

    private static class ArenaSaveListener implements Listener {

        @EventHandler
        public void on(PluginDisableEvent event) {
            if (event.getPlugin().equals(Plugin.get())) {
                ArenaTracker.INSTANCE.save();
            }
        }
    }
}
