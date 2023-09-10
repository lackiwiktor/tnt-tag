package me.ponktacology.tag.game;

import me.ponktacology.tag.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Ticker {

    private BukkitTask task;
    private final Runnable onTick;

    public Ticker(Runnable onTick) {
        this.onTick = onTick;
    }

    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                onTick.run();
            }
        }.runTaskTimer(Plugin.get(), 1L, 1L);
    }

    public void cancel() {
        if (task != null) task.cancel();
    }
}
