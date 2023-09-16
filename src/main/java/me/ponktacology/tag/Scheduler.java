package me.ponktacology.tag;

import org.bukkit.Bukkit;

public class Scheduler {

    public static void async(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.get(), runnable);
    }
}
