package me.ponktacology.tag.statistics;

import me.ponktacology.tag.Database;
import me.ponktacology.tag.game.Statistic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Statistics {

    private final UUID player;
    private final Map<Statistic.Type, Statistic> statistics = new HashMap<>();

    public Statistics(UUID player) {
        this.player = player;
    }

    public void increment(Statistic.Type type, int incrementBy) {
        final var statistic = statistics.computeIfAbsent(type, t -> new Statistic(t, 0));
        statistic.increment(incrementBy);
    }

    public int get(Statistic.Type type) {
        return statistics.computeIfAbsent(type, t -> new Statistic(t, 0)).value();
    }

    public void save() {
        // Terrible, n+1 problem, please fix!
        for (Statistic statistic : statistics.values()) {
            if (!statistic.dirty()) continue;
            final var affectedRows = Database.INSTANCE.update("UPDATE SET value = ? WHERE id = ? AND type = ?", statement -> {
                try {
                    statement.setInt(1, statistic.value());
                    statement.setString(2, player.toString());
                    statement.setString(3, statistic.type().toString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            System.out.println(affectedRows);
        }
    }

    public void fetch() {
        Database.INSTANCE.query("SELECT * FROM statistics WHERE id = ?", statement -> {
            try {
                statement.setString(1, player.toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, result -> {
            while (true) {
                try {
                    if (!result.next()) break;
                    final var type = Statistic.Type.valueOf(result.getString(3));
                    final var value = result.getInt(4);
                    statistics.put(type, new Statistic(type, value));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            // Terrible, n+1 problem, please fix!
            for (Statistic.Type type : Statistic.Type.values()) {
                if (statistics.containsKey(type)) continue;
                Database.INSTANCE.update("INSERT INTO statistics (id, type, value) VALUES (?, ?, ?)", statement -> {
                    try {
                        statement.setString(1, player.toString());
                        statement.setString(2, type.toString());
                        statement.setInt(3, 0);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });


    }
}
