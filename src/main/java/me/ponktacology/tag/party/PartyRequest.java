package me.ponktacology.tag.party;

import me.ponktacology.tag.Constants;

import java.util.Objects;
import java.util.UUID;

public class PartyRequest {
    private final UUID sender;
    private final long invitedOn = System.currentTimeMillis();

    public PartyRequest(UUID sender) {
        this.sender = sender;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() - invitedOn > Constants.PARTY_INVITE_EVICTION_TIME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartyRequest that = (PartyRequest) o;
        return Objects.equals(sender, that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender);
    }
}
