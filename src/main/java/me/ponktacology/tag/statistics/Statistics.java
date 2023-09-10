package me.ponktacology.tag.statistics;

import me.ponktacology.tag.game.Statistic;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

    private final Map<Statistic.Type, Statistic> statistics = new HashMap<>();

    public void increment(Statistic.Type type, int incrementBy) {
        statistics.getOrDefault(type, new Statistic(type, incrementBy));
    }
}
