package io.insomniac.skygrid2.selectors;

import net.minecraft.util.RandomSource;
import java.util.NavigableMap;
import java.util.TreeMap;

/// Reusable, high-performance weighted selection using a Cumulative Distribution Function (CDF).
public class DynamicWeightedSelector<T> {
    private final NavigableMap<Double, T> weights = new TreeMap<>();
    private double totalWeight = 0.0;

    public void add(double weight, T item) {
        if (weight <= 0) return;
        totalWeight += weight;
        weights.put(totalWeight, item);
    }

    public boolean isEmpty() {
        return weights.isEmpty();
    }

    public T getRandom(RandomSource random, T fallback) {
        if (weights.isEmpty()) return fallback;
        double value = random.nextDouble() * totalWeight;
        var entry = weights.higherEntry(value);
        return entry != null ? entry.getValue() : weights.lastEntry().getValue();
    }
}
