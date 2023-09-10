package me.ponktacology.tag.game;

import me.ponktacology.tag.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class Countdown {

    private final Runnable finishRunnable;
    private final Consumer<Integer> countdownConsumer;
    private final int duration;
    private int elapsed;
    private BukkitTask task;

    public Countdown(Runnable finishRunnable, Consumer<Integer> countdownConsumer, int duration) {
        this.finishRunnable = finishRunnable;
        this.countdownConsumer = countdownConsumer;
        this.duration = duration;
        this.elapsed = duration;
    }


    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (elapsed <= 0) {
                    finishRunnable.run();
                    cancel();
                    return;
                }

                countdownConsumer.accept(elapsed--);
            }
        }.runTaskTimer(Plugin.get(), 0, 20);
    }

    public void cancel() {
        if (task != null) task.cancel();
        elapsed = duration;
    }
}
