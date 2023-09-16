package me.ponktacology.tag.statistics;

import me.vaperion.blade.annotation.argument.Name;
import me.vaperion.blade.annotation.argument.Optional;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import me.vaperion.blade.annotation.command.Description;
import org.bukkit.entity.Player;

public class StatisticsCommands {

    @Command("statistics")
    @Description("Shows statistics")
    public static void statistics(@Sender Player sender, @Name("target") @Optional("self") Player target) {
        StatisticsTracker.INSTANCE.print(sender, target);
    }
}
