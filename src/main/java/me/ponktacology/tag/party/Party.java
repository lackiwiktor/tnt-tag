package me.ponktacology.tag.party;

import me.ponktacology.tag.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Party {

    private class PartyInvite {
        private final UUID player;
        private final long invitedOn = System.currentTimeMillis();

        private PartyInvite(UUID player) {
            this.player = player;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() - invitedOn > Constants.PARTY_INVITE_EVICTION_TIME;
        }
    }

    private final UUID id = UUID.randomUUID();
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<PartyInvite> invites = new HashSet<>();
    private final int maxSize;

    public Party(Player player) {
        this.leader = player.getUniqueId();
        this.members.add(leader);
        this.maxSize = Constants.getMaxPartySize(player);
    }

    public void addMember(Player player) {
        invites.removeIf(it -> it.player.equals(player.getUniqueId()));
        members.add(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return members.contains(player.getUniqueId());
    }

    public void createInvite(Player player) {
        invites.add(new PartyInvite(player.getUniqueId()));
    }

    public boolean hasInvited(Player player) {
        for (PartyInvite invite : invites) {
            if (!invite.hasExpired() && invite.player.equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isLeader(Player actor) {
        return leader.equals(actor.getUniqueId());
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    public List<Player> getOnlineMembers() {
        return members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void evictRequests() {
        invites.removeIf(PartyInvite::hasExpired);
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", leader=" + leader +
                ", members=" + members +
                ", invites=" + invites +
                ", maxSize=" + maxSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party party = (Party) o;
        return Objects.equals(id, party.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
