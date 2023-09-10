package me.ponktacology.tag.game;

import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommands {

    @Command("joinqueue")
    public static void join(@Sender Player sender) {
        GameTracker.INSTANCE.joinQueue(sender);
    }

    @Command("game list")
    public static void list(@Sender CommandSender sender) {
        GameTracker.INSTANCE.games().forEach(it -> sender.sendMessage(it.toString()));
    }

}
