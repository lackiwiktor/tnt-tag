package me.ponktacology.tag.party;

import me.vaperion.blade.annotation.argument.Name;
import me.vaperion.blade.annotation.argument.Optional;
import me.vaperion.blade.annotation.argument.Sender;
import me.vaperion.blade.annotation.command.Command;
import me.vaperion.blade.annotation.command.Description;
import org.bukkit.entity.Player;

public class PartyCommands {

    @Command("party game")
    @Description("Starts a party game")
    public static void game(@Sender Player actor) {
        PartyTracker.INSTANCE.startGame(actor);
    }

    @Command("party info")
    @Description("Shows info about party")
    public static void info(@Sender Player actor, @Name("party") @Optional("me") Party party) {
        actor.sendMessage(party.toString());
    }

    @Command("party create")
    @Description("Creates a party")
    public static void create(@Sender Player actor) {
        PartyTracker.INSTANCE.createParty(actor);
    }

    @Command("party invite")
    @Description("Invites to the party")
    public static void create(@Sender Player actor, @Name("target") Player other) {
        PartyTracker.INSTANCE.inviteToParty(actor, other);
    }

    @Command({"party join", "party accept"})
    @Description("Joins the party")
    public static void create(@Sender Player actor, @Name("party") Party party) {
        PartyTracker.INSTANCE.acceptInvite(actor, party);
    }

    @Command("party leave")
    @Description("Leaves the party")
    public static void leave(@Sender Player actor) {
        PartyTracker.INSTANCE.leave(actor);
    }

    @Command("party disband")
    @Description("Disbands the party")
    public static void disband(@Sender Player actor) {
        PartyTracker.INSTANCE.disband(actor);
    }

    @Command("party kick")
    @Description("Kicks a player from the party")
    public static void kick(@Sender Player actor, @Name("target") Player target) {
        PartyTracker.INSTANCE.kick(actor, target);
    }

}
