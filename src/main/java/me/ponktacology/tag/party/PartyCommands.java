package me.ponktacology.tag.party;

import me.vaperion.blade.annotation.argument.Name;
import me.vaperion.blade.annotation.argument.Optional;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import org.bukkit.entity.Player;

public class PartyCommands {

    @Command("party game")
    public static void game(@Sender Player actor) {
        PartyTracker.INSTANCE.startGame(actor);
    }

    @Command("party info")
    public static void info(@Sender Player actor, @Name("party") @Optional("me") Party party) {
        actor.sendMessage(party.toString());
    }

    @Command("party create")
    public static void create(@Sender Player actor) {
        PartyTracker.INSTANCE.createParty(actor);
    }

    @Command("party invite")
    public static void create(@Sender Player actor, @Name("target") Player other) {
        PartyTracker.INSTANCE.inviteToParty(actor, other);
    }

    @Command("party join")
    public static void create(@Sender Player actor, @Name("party") Party party) {
        PartyTracker.INSTANCE.acceptInvite(actor, party);
    }

    @Command("party leave")
    public static void leave(@Sender Player actor) {
        PartyTracker.INSTANCE.leave(actor);
    }

    @Command("party disband")
    public static void disband(@Sender Player actor) {
        PartyTracker.INSTANCE.disband(actor);
    }

    @Command("party kick")
    public static void kick(@Sender Player actor, @Name("target") Player target) {
        PartyTracker.INSTANCE.kick(actor, target);
    }

}
