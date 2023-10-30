package me.ponktacology.tag.arena;

import me.vaperion.blade.argument.Argument;
import me.vaperion.blade.argument.ArgumentProvider;
import me.vaperion.blade.context.Context;
import me.vaperion.blade.exception.BladeExitMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ArenaArgumentProvider implements ArgumentProvider<Arena> {

    @Override
    public @Nullable Arena provide(@NotNull Context context, @NotNull Argument argument) throws BladeExitMessage {
        final var arena = ArenaTracker.INSTANCE.getById(argument.getString());
        if (arena == null) throw new BladeExitMessage("Arena is not found.");
        return arena;
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Context context, @NotNull Argument argument) throws BladeExitMessage {
        return ArenaTracker.INSTANCE.getAll()
                .stream()
                .map(Arena::getId)
                .filter(it -> it.toLowerCase(Locale.ROOT).startsWith(argument.getString().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }
}

