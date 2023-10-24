package me.ponktacology.tag.map;

import me.vaperion.blade.annotation.argument.Name;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommands {

    @Command("arena create")
    public void create(@Sender CommandSender sender, @Name("id") String id) {
        final var arena = ArenaTracker.INSTANCE.getById(id);
        if (arena != null) {
            sender.sendMessage("Arena with this id already exists.");
            return;
        }
        ArenaTracker.INSTANCE.create(id);
        sender.sendMessage("Successfully created the arena.");
    }

    @Command("arena lobby")
    public void lobby(@Sender Player sender, @Name("arena") Arena arena) {
        arena.setLobbySpawn(sender.getLocation());
        sender.sendMessage("Successfully set lobby location of the arena.");
    }

    @Command("arena game")
    public void game(@Sender Player sender, @Name("arena") Arena arena) {
        arena.setGameSpawn(sender.getLocation());
        sender.sendMessage("Successfully set game location of the arena.");
    }

    @Command("arena displayName")
    public void game(@Sender Player sender, @Name("arena") Arena arena, @Name("displayName") String displayName) {
        arena.setDisplayName(displayName);
        sender.sendMessage("Successfully set display name of the arena.");
    }
}
