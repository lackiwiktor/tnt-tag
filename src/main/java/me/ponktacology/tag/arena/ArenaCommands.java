package me.ponktacology.tag.arena;

import me.vaperion.blade.annotation.argument.Name;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import me.vaperion.blade.annotation.command.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommands {

    @Command("arena create")
    @Permission("arena.admin")
    public static void create(@Sender CommandSender sender, @Name("id") String id) {
        final var arena = ArenaTracker.INSTANCE.getById(id);
        if (arena != null) {
            sender.sendMessage("Arena with this id already exists.");
            return;
        }
        ArenaTracker.INSTANCE.create(id);
        sender.sendMessage("Successfully created the arena.");
    }

    @Command("arena delete")
    @Permission("arena.admin")
    public static void delete(@Sender CommandSender sender, @Name("arena") Arena arena) {
        ArenaTracker.INSTANCE.delete(arena);
        sender.sendMessage("Successfully deleted the arena.");
    }

    @Command("arena lobby")
    @Permission("arena.admin")
    public static void lobby(@Sender Player sender, @Name("arena") Arena arena) {
        arena.setLobbySpawn(sender.getLocation());
        sender.sendMessage("Successfully set lobby location of the arena.");
    }


    @Command("arena game")
    @Permission("arena.admin")
    public static void game(@Sender Player sender, @Name("arena") Arena arena) {
        arena.setGameSpawn(sender.getLocation());
        sender.sendMessage("Successfully set game location of the arena.");
    }

    @Command("arena displayName")
    @Permission("arena.admin")
    public static void game(@Sender Player sender, @Name("arena") Arena arena, @Name("displayName") String displayName) {
        arena.setDisplayName(displayName);
        sender.sendMessage("Successfully set display name of the arena.");
    }
}
