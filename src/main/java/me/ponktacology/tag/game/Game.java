package me.ponktacology.tag.game;

import me.ponktacology.tag.Constants;
import me.ponktacology.tag.Visibility;
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

import static me.ponktacology.tag.Constants.NEAREST_PLAYER_COMPASS_UPDATE_DELAY;

public class Game {

    public static final NumberFormat TIMER_FORMAT = new DecimalFormat("0.0#");

    private enum State {WAITING_FOR_PLAYERS, COUNTDOWN, ROUND_START, ROUND_END, FINISHED, CANCELLED}

    private final UUID id = UUID.randomUUID();
    private final Participant[] podium = new Participant[3];
    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<UUID, Spectator> spectators = new HashMap<>();
    private final Ticker logic = createTicker();
    private final Visibility.Strategy visibilityStrategy = createVisibilityStrategy();
    private final Countdown countdown = createCountdown();
    private final Consumer<Game> finishCallback;
    private final boolean privateGame;

    private State state = State.WAITING_FOR_PLAYERS;

    private int ticks;
    private int round;
    private long roundStart;

    public Game(boolean privateGame, Consumer<Game> finishCallback) {
        this.privateGame = privateGame;
        this.finishCallback = finishCallback;
    }

    public void start() {
        logic.start();
    }

    private Ticker createTicker() {
        return new Ticker(() -> {
            ticks++;
            if (state == State.WAITING_FOR_PLAYERS) {
                handleWaitingForPlayers();
                return;
            }

            if (state == State.ROUND_START) {
                if (hasRoundEnded()) {
                    endRound();
                    return;
                }

                if (ticks % Constants.NEAREST_PLAYER_COMPASS_UPDATE_DELAY == 0) {
                    updateCompass();
                }
            }
        });
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

    private Countdown createCountdown() {
        return new Countdown(() -> {
            participants().forEach(Participant::prepareForGame); //Teleport players only on initial round start
            startRound();
        }, seconds -> {
            if (participants.size() < Constants.REQUIRED_PLAYERS) {
                broadcast("Waiting for players...");
                countdown.cancel();
                state = State.WAITING_FOR_PLAYERS;
                return;
            }
            broadcast("Game is starting in " + seconds + "!");
        }, Constants.COUNTDOWN_DURATION);
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

    private void handleWaitingForPlayers() {
        final var playerCount = participants.size();

        if (playerCount >= Constants.REQUIRED_PLAYERS) {
            startCountdown();
        }
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
        participant.prepareForLobby(visibilityStrategy);
        broadcast(player.getDisplayName() + " joined (" + participants.size() + "/" + Constants.MAX_PLAYERS + ")");
        return true;
    }

    private void startCountdown() {
        state = State.COUNTDOWN;
        countdown.start();
    }

    private void startRound() {
        round++;
        roundStart = System.currentTimeMillis();
        state = State.ROUND_START;
        final var participants = new ArrayList<>(participants());
        Collections.shuffle(participants);
        var initiallyTaggedCount = Math.max(participants.size() * Constants.INITIALLY_TAGGED_FACTOR, 1);
        for (Participant participant : participants) {
            if (initiallyTaggedCount-- <= 0) break;
            participant.markAsTagged();
        }
    }

    private void endRound() {
        state = State.ROUND_END;
        List<Participant> losers = new ArrayList<>();
        List<Participant> winners = new ArrayList<>();
        final var participantIterator = participants.entrySet().iterator();

        while (participantIterator.hasNext()) {
            final var participantEntry = participantIterator.next();
            final var participant = participantEntry.getValue();
            if (!participant.isTagged()) {
                winners.add(participant);
                continue;
            }

            if (!privateGame) StatisticsTracker.INSTANCE.incrementByOne(participant.getUUID(), Statistic.Type.LOOSE);

            broadcast(participant.getName() + " exploded!");
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
            if (!privateGame) StatisticsTracker.INSTANCE.incrementByOne(killer.getUUID(), Statistic.Type.KILL);
            assisters.remove(killer);
        }
        for (Participant assister : assisters) {
            if (!privateGame) StatisticsTracker.INSTANCE.incrementByOne(assister.getUUID(), Statistic.Type.KILL);
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

        broadcast(losers.stream().map(Participant::getName).collect(Collectors.joining(", ")) + " are losers!");
        startRound();
    }

    private void endGame(Participant winner) {
        if (!privateGame) StatisticsTracker.INSTANCE.incrementByOne(winner.getUUID(), Statistic.Type.WIN);
        for (int i = 0; i < podium.length; i++) {
            final var participant = podium[i];
            if (participant == null) continue;
            broadcast((i + 1) + "# " + participant.getName());
        }
        finishGame();
    }

    private void finishGame() {
        state = State.FINISHED;
        logic.cancel();
        countdown.cancel();
        spectators().forEach(Spectator::moveToHub);
        participants().forEach(Participant::moveToHub);
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

        if (hasRoundEnded() || state != State.ROUND_START) {
            event.setCancelled(true);
            return true;
        }

        if (!victim.isTagged() && attacker.isTagged()) {
            victim.markAsTagged(attacker);
            attacker.markAsNotTagged();
            broadcast(victim.getName() + " is now IT.");
            if (!privateGame) {
                StatisticsTracker.INSTANCE.incrementByOne(attacker.getUUID(), Statistic.Type.HIT);
                StatisticsTracker.INSTANCE.incrementByOne(victim.getUUID(), Statistic.Type.DAMAGED);
            }
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
            final var tagged = participants().stream().filter(it -> it.isTagged() && it.getTaggedBy() == participant).findFirst().orElse(null);
            if (tagged != null) tagged.markAsNotTagged(); //Remove one TNT from the game
        }

        broadcast(participant.getName() + " quit.");

        return true;
    }

    private void addSpectator(Participant participant) {
        final var spectator = participant.toSpectator();
        spectators.put(participant.getUUID(), spectator);
        spectator.setup(visibilityStrategy);
    }

    public boolean isPrivate() {
        return privateGame;
    }

    private Collection<Participant> participants() {
        return participants.values();
    }

    private Collection<Spectator> spectators() {
        return spectators.values();
    }

    public long roundTimeLeft() {
        return state == State.ROUND_START ? roundDuration() - (System.currentTimeMillis() - roundStart) : 0;
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

    public List<String> scoreboard(Player player) {
        switch (state) {
            case WAITING_FOR_PLAYERS:
            case COUNTDOWN:
                return List.of("Players: " + participants.size() + "/" + Constants.MAX_PLAYERS);
            case ROUND_START:
                return List.of("Round: " + round, "Explosion in: " + TIMER_FORMAT.format(roundTimeLeft() / 1000.0), "Alive: " + participants.size());
            default:
                return List.of("Game finished!");
        }
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
