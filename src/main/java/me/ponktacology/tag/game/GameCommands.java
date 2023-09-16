package me.ponktacology.tag.game;

import me.ponktacology.tag.Hub;
import me.vaperion.blade.annotation.argument.Flag;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import me.vaperion.blade.annotation.command.Description;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommands {

    @Command("game join")
    @Description("Joins the game")
    public static void join(@Sender Player sender) {
        GameTracker.INSTANCE.joinQueue(sender);
    }

    @Command("game list")
    @Description("Lists all running games")
    public static void list(@Sender CommandSender sender, @Flag('f') boolean full) {
        sender.sendMessage("Active games: " + GameTracker.INSTANCE.games().size());
        if (full) GameTracker.INSTANCE.games().forEach(it -> sender.sendMessage(it.toString()));
    }

    @Command("game leave")
    public static void leave(@Sender Player sender) {
        final var game = GameTracker.INSTANCE.getByPlayer(sender);
        if (game == null) {
            sender.sendMessage("You are not in a game");
            return;
        }
        game.handleQuit(sender);
        Hub.INSTANCE.moveToHub(sender);
    }

    @Command("game requeue")
    public static void requeue(@Sender Player sender) {
        final var game = GameTracker.INSTANCE.getByPlayer(sender);
        if (game != null) {
            game.handleQuit(sender);
            return;
        }
        GameTracker.INSTANCE.joinQueue(sender);
    }
}
