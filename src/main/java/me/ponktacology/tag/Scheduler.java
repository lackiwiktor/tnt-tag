package me.ponktacology.tag;

import org.bukkit.Bukkit;

public class Scheduler {

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.get(), runnable);
    }

    public static void later(Runnable runnable, int ticks) {
        Bukkit.getScheduler().runTaskLater(Plugin.get(), runnable, ticks);
    }

    public static void timer(Runnable runnable, int delay) {
        Bukkit.getScheduler().runTaskTimer(Plugin.get(), runnable, 0, delay);
    }
}
