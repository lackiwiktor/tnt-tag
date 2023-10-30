package me.ponktacology.tag.game;

import me.ponktacology.tag.Constants;
import me.ponktacology.tag.Messages;
import me.ponktacology.tag.Visibility;
import me.ponktacology.tag.arena.Arena;
import me.ponktacology.tag.party.PartyTracker;
import me.ponktacology.tag.statistics.StatisticsTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Game {

    public static final NumberFormat TIMER_FORMAT = new DecimalFormat("0.0");

    private enum State {WAITING_FOR_PLAYERS, COUNTDOWN, ROUND_RUNNING, ROUND_END, GAME_END, FINISHED, CANCELLED}

    private final UUID id = UUID.randomUUID();
    private final Participant[] podium = new Participant[3];
    private final Map<UUID, Participant> participants = new HashMap<>();
    // Perhaps it'd be better to store participants and spectators in the same collection?
    private final Map<UUID, Spectator> spectators = new HashMap<>();
    private final Logic logic = createLogic();
    private final Visibility.Strategy visibilityStrategy = createVisibilityStrategy();
    private final Arena arena;
    private final Consumer<Game> finishCallback;
    private final boolean privateGame;
    private State state = State.WAITING_FOR_PLAYERS;
    private int ticks;
    private int round;
    private long roundStart;

    public Game(Arena arena, boolean privateGame, Consumer<Game> finishCallback) {
        this.arena = arena;
        this.privateGame = privateGame;
        this.finishCallback = finishCallback;
    }

    public void start() {
        logic.start();
    }

    private Logic createLogic() {
        return new Logic(() -> {
            switch (state) {
                case WAITING_FOR_PLAYERS:
                    waitForPlayers();
                    break;
                case COUNTDOWN:
                    countDown();
                    break;
                case ROUND_RUNNING:
                    if (hasRoundEnded()) {
                        endRound();
                        return;
                    }
                    if (ticks % Constants.NEAREST_PLAYER_COMPASS_UPDATE_DELAY == 0) {
                        updateCompass();
                    }
                    break;
                case ROUND_END:
                    if (ticks >= Constants.DELAY_BETWEEN_ROUNDS) {
                        startRound();
                    }
                    break;
                case GAME_END:
                    if (ticks >= Constants.DELAY_AFTER_GAME_END) {
                        finishGame();
                    }
                    break;
            }

            ticks++;
        });
    }

    private void waitForPlayers() {
        if (participants.size() >= Constants.REQUIRED_PLAYERS) {
            state = State.COUNTDOWN;
            ticks = -1;
        }
    }

    private void countDown() {
        if (participants.size() < Constants.REQUIRED_PLAYERS) {
            state = State.WAITING_FOR_PLAYERS;
            broadcast(Messages.get("waiting_for_players"));
            return;
        }

        if (ticks % 20 != 0) {
            return;
        }

        final var timeLeft = (Constants.COUNTDOWN_DURATION - ticks) / 20;
        if (timeLeft <= 0) {
            participants().forEach(participant -> participant.prepareForGame(arena)); //Teleport players only on initial round start
            startRound();
        } else broadcast(Messages.get("game_starts_in")
                .replace("{time}", String.valueOf(timeLeft)));
    }

    private void updateCompass() {
        final var participants = participants();
        for (Participant participant : participants) {
            var minDistance = Double.MAX_VALUE;
            var closestPlayer = participant;
            for (Participant other : participants) {
                if (participant.equals(other)) continue;
                final var distance = participant.getLocation().distanceSquared(other.getLocation());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPlayer = other;
                }
            }
            participant.setCompass(closestPlayer.getLocation());
        }
    }


    private Visibility.Strategy createVisibilityStrategy() {
        return (player, other) -> {
            final var participant = participants.get(other.getUniqueId());

            if (participant == null) {
                final var spectator = spectators.get(other.getUniqueId());
                if (spectator == null) return false;
                return spectators.get(player.getUniqueId()) != null;
            }

            return participants.get(player.getUniqueId()) != null || spectators.get(player.getUniqueId()) != null;
        };
    }


    public boolean join(Player player) {
        if (state != State.WAITING_FOR_PLAYERS && state != State.COUNTDOWN) {
            //  player.sendMessage("Game has already started.");
            return false;
        }

        if (participants.size() >= Constants.MAX_PLAYERS) {
            //   player.sendMessage("Game is full.");
            return false;
        }

        final var party = PartyTracker.INSTANCE.getByPlayer(player);
        if (party != null && party.isLeader(player)) {
            final var onlinePlayers = party.getOnlineMembers();
            if (participants.size() + onlinePlayers.size() > Constants.MAX_PLAYERS) {
                // player.sendMessage("Game is full.");
                return false;
            }

            for (Player onlinePlayer : onlinePlayers) {
                if (onlinePlayer == player) continue;
                final var game = GameTracker.INSTANCE.getByPlayer(onlinePlayer);
                if (game != null) game.handleQuit(onlinePlayer);
                join(onlinePlayer);
            }
        }

        final var participant = new Participant(player.getUniqueId());
        participants.put(player.getUniqueId(), participant);
        GameTracker.INSTANCE.addPlayer(player, this);
        participant.prepareForLobby(arena, visibilityStrategy);
        broadcast(Messages.get("player_joined")
                .replace("{player}", participant.getName())
                .replace("{game_size}", String.valueOf(participants.size()))
                .replace("{game_max_size}", String.valueOf(Constants.MAX_PLAYERS)));
        return true;
    }

    private void startRound() {
        round++;
        roundStart = System.currentTimeMillis();
        state = State.ROUND_RUNNING;
        final var participants = new ArrayList<>(participants());
        Collections.shuffle(participants);
        var initiallyTaggedCount = Math.max(participants.size() * Constants.INITIALLY_TAGGED_FACTOR, 1);

        for (Participant participant : participants) {
            if (initiallyTaggedCount-- <= 0) break;
            participant.markAsTagged();
        }
    }

    private void endRound() {
        final var losers = new ArrayList<Participant>();
        final var winners = new ArrayList<Participant>();
        final var participantIterator = participants.entrySet().iterator();

        while (participantIterator.hasNext()) {
            final var participantEntry = participantIterator.next();
            final var participant = participantEntry.getValue();
            if (!participant.isTagged()) {
                winners.add(participant);
                continue;
            }

            incrementStatistic(participant, Statistic.Type.LOOSE);
            broadcast(Messages.get("player_exploded")
                    .replace("{player}", participant.getName()));
            losers.add(participant);
            participantIterator.remove();
            addSpectator(participant);
        }

        final var killers = losers.stream().map(Participant::getTaggedBy).filter(Objects::nonNull).collect(Collectors.toList());
        final var assisters = new HashSet<Participant>();

        for (Participant loser : losers) {
            assisters.addAll(loser.getAssisters());
        }

        for (Participant killer : killers) {
            incrementStatistic(killer, Statistic.Type.KILL);
            assisters.remove(killer);
        }

        for (Participant assister : assisters) {
            incrementStatistic(assister, Statistic.Type.ASSIST);
        }

        if (winners.size() <= podium.length && winners.size() > 1) {
            podium[winners.size() - 1] = losers.get(0);
        }

        if (winners.size() == 1) {
            final var winner = winners.get(0);
            podium[0] = winner;
            endGame(winner);
            return;
        }

        ticks = -1;
        state = State.ROUND_END;

        broadcast(Messages.get("round_end").replace("losers", losers.stream().map(Participant::getName).collect(Collectors.joining(", "))));
    }

    private void endGame(Participant winner) {
        incrementStatistic(winner, Statistic.Type.WIN);
        for (int i = 0; i < podium.length; i++) {
            final var participant = podium[i];
            if (participant == null) continue;
            broadcast((i + 1) + "# " + participant.getName());
        }
        ticks = -1;
        state = State.GAME_END;
    }

    private void finishGame() {
        state = State.FINISHED;
        logic.cancel();
        spectators().forEach(spectator -> {
            GameTracker.INSTANCE.removePlayer(spectator.getUUID());
            spectator.moveToHub();
        });
        participants().forEach(participant -> {
            GameTracker.INSTANCE.removePlayer(participant.getUUID());
            participant.moveToHub();
        });
        finishCallback.accept(this);
    }

    public boolean handleCombat(EntityDamageByEntityEvent event) {
        final var victim = participants.get(event.getEntity().getUniqueId());
        if (victim == null) {
            if (spectators.containsKey(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
                return true;
            }
            return false;
        }
        final var attacker = participants.get(event.getDamager().getUniqueId());
        if (attacker == null) {
            if (spectators.containsKey(event.getDamager().getUniqueId())) {
                event.setCancelled(true);
                return true;
            }
            return false;
        }

        if (hasRoundEnded() || state != State.ROUND_RUNNING) {
            event.setCancelled(true);
            return true;
        }

        if (!victim.isTagged() && attacker.isTagged()) {
            victim.markAsTagged(attacker);
            attacker.markAsNotTagged();
            broadcast(Messages.get("player_tag").replace("{player}", victim.getName()));

            incrementStatistic(attacker, Statistic.Type.HIT);
            incrementStatistic(victim, Statistic.Type.DAMAGED);
        }

        event.setDamage(0.01);
        return true;
    }

    public boolean handleDamage(EntityDamageEvent event) {
        final var victim = participants.get(event.getEntity().getUniqueId());
        if (victim == null) return false;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);
        return true;
    }

    public boolean handleQuit(Player player) {
        final var participant = participants.remove(player.getUniqueId());

        if (participant == null) {
            return spectators.remove(player.getUniqueId()) != null;
        }

        if (participant.hasTaggedRecently()) {
            participants().stream()
                    .filter(it -> it.isTagged() && it.getTaggedBy() == participant)
                    .findFirst()
                    .ifPresent(Participant::markAsNotTagged);
        }

        broadcast(Messages.get("player_quit").replace("{player_quit}", participant.getName()));

        GameTracker.INSTANCE.removePlayer(player.getUniqueId());

        return true;
    }

    private void addSpectator(Participant participant) {
        final var spectator = participant.toSpectator();
        spectators.put(participant.getUUID(), spectator);
        spectator.setup(arena, visibilityStrategy);
    }

    public boolean isPrivate() {
        return privateGame;
    }

    private Collection<Participant> participants() {
        return participants.values();
    }

    public Collection<Spectator> spectators() {
        return spectators.values();
    }

    public long roundTimeLeft() {
        return Math.max(roundDuration() - (System.currentTimeMillis() - roundStart), 0);
    }

    private void broadcast(String message) {
        participants().forEach(it -> it.sendMessage(message));
        spectators().forEach(it -> it.sendMessage(message));
    }

    private boolean hasRoundEnded() {
        return roundTimeLeft() <= 0;
    }

    public boolean isInGame(Player player) {
        return participants.containsKey(player.getUniqueId()) || spectators.containsKey(player.getUniqueId());
    }

    private long roundDuration() {
        return round == 1 ? Constants.FIRST_ROUND_DURATION : Constants.ROUND_DURATION;
    }

    private void incrementStatistic(Participant participant, Statistic.Type type) {
        if (privateGame) return;
        StatisticsTracker.INSTANCE.incrementByOne(participant.getUUID(), type);
    }

    public List<String> scoreboard(Player player) {
        switch (state) {
            case WAITING_FOR_PLAYERS:
            case COUNTDOWN:
                return List.of("Arena: " + arena.getDisplayName(), "Players: " + participants.size() + "/" + Constants.MAX_PLAYERS);
            case ROUND_RUNNING:
                return List.of("Round: " + round, "Explosion in: " + TIMER_FORMAT.format(roundTimeLeft() / 1000.0), "Alive: " + participants.size());
            default:
                return List.of("Game finished!");
        }
    }

    public boolean isFinished() {
        return state == State.FINISHED;
    }

    @Override
    public String toString() {
        return "Game{" + "participants=" + participants + ", spectators=" + spectators + ", state=" + state + ", roundStart=" + roundStart + ", timeLeft=" + roundTimeLeft() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
