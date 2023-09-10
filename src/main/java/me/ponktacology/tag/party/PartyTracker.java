package me.ponktacology.tag.party;

import me.ponktacology.tag.Hub;
import me.ponktacology.tag.game.GameTracker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public enum PartyTracker {
    INSTANCE;

    private final Set<Party> parties = new HashSet<>();

    public boolean startGame(Player actor) {
        final var party = getByPlayer(actor);
        if (party == null) {
            actor.sendMessage("You are not in a party.");
            return false;
        }

        if (!party.isLeader(actor)) {
            actor.sendMessage("You are not the party leader.");
            return false;
        }

        if (!Hub.INSTANCE.isInHub(actor)) {
            actor.sendMessage("You must be in a hub in order to start a party game.");
            return false;
        }

        final var game = GameTracker.INSTANCE.start(true);
        return game.join(actor);
    }

    public boolean createParty(Player player) {
        if (!Hub.INSTANCE.isInHub(player)) {
            player.sendMessage("You must be in a hub in order to create a party.");
            return false;
        }

        if (getByPlayer(player) != null) {
            player.sendMessage("You are already in a party.");
            return false;
        }

        parties.add(new Party(player));
        player.sendMessage("Successfully created a party.");
        return true;
    }

    public boolean inviteToParty(Player actor, Player player) {
        final var party = getByPlayer(actor);
        if (party == null) {
            actor.sendMessage("You are not in a party.");
            return false;
        }

        if (getByPlayer(player) != null) {
            actor.sendMessage("Player is already in a party.");
            return false;
        }

        if (party.hasInvited(player)) {
            actor.sendMessage("This player is already invited.");
            return false;
        }

        party.createInvite(player);
        actor.sendMessage("Successfully invited " + player.getName() + " to your party.");
        player.sendMessage("You have been invited to " + actor.getName() + "'s party.");
        player.sendMessage("Click here to accept.");
        return true;
    }

    public boolean acceptInvite(Player actor, Party party) {
        if (getByPlayer(actor) != null) {
            actor.sendMessage("You are already in a party.");
            return false;
        }

        if (!Hub.INSTANCE.isInHub(actor)) {
            actor.sendMessage("You must be in a hub in order to join a party.");
            return false;
        }

        if (!party.hasInvited(actor)) {
            actor.sendMessage("You wasn't invited to this party.");
            return false;
        }

        if (party.isFull()) {
            actor.sendMessage("Party is full.");
            return false;
        }

        party.addMember(actor);
        actor.sendMessage("Successfully joined the party.");
        return true;
    }

    public boolean disband(Player actor) {
        final var party = getByPlayer(actor);
        if (party == null) {
            actor.sendMessage("You are not in a party.");
            return false;
        }
        if (!party.isLeader(actor)) {
            actor.sendMessage("You must be the leader of the party to disband it.");
            return false;
        }

        parties.remove(party);
        actor.sendMessage("Successfully disbanded party.");
        return true;
    }

    public boolean kick(Player actor, Player player) {
        final var party = getByPlayer(actor);
        if (party == null) {
            actor.sendMessage("You are not in a party.");
            return false;
        }

        if (!party.isLeader(actor)) {
            actor.sendMessage("You must be the leader of the party to kick someone from it.");
            return false;
        }

        if (!party.hasPlayer(player)) {
            actor.sendMessage("This player is not in your party.");
            return false;
        }

        party.removeMember(player);
        actor.sendMessage("Successfully kicked the player.");
        return true;
    }

    public boolean leave(Player actor) {
        final var party = getByPlayer(actor);
        if (party == null) {
            actor.sendMessage("You are not in a party.");
            return false;
        }

        if (party.isLeader(actor)) {
            actor.sendMessage("You must disband your party before leaving as a leader.");
            return false;
        }

        party.removeMember(actor);
        actor.sendMessage("Successfully left the party.");
        return true;
    }

    public @Nullable Party getByPlayer(Player player) {
        for (Party party : parties) {
            if (party.hasPlayer(player)) return party;
        }

        return null;
    }

    public Set<Party> parties() {
        return parties;
    }
}
