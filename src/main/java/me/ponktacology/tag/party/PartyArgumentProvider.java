package me.ponktacology.tag.party;

import me.vaperion.blade.argument.Argument;
import me.vaperion.blade.argument.ArgumentProvider;
import me.vaperion.blade.context.Context;
import me.vaperion.blade.exception.BladeExitMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PartyArgumentProvider implements ArgumentProvider<Party> {

    @Override
    public @Nullable Party provide(@NotNull Context context, @NotNull Argument argument) throws BladeExitMessage {
        final var player = Bukkit.getPlayer(argument.getString());
        for (Party party : PartyTracker.INSTANCE.parties()) {
            if (party.hasPlayer(player)) return party;
        }
        throw new BladeExitMessage("Party not found.");
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Context context, @NotNull Argument argument) throws BladeExitMessage {
        return Bukkit.getOnlinePlayers().stream()
                .map(HumanEntity::getName)
                .filter(it -> it.toLowerCase(Locale.ROOT).startsWith(argument.getString().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}

