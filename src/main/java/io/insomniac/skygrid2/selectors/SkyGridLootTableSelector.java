package io.insomniac.skygrid2.selectors;

import io.insomniac.skygrid2.SkyGridConfig;
import io.insomniac.skygrid2.SkyGridConfigManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

/// LootTables are handled differently for now
public class SkyGridLootTableSelector {
    private static final NavigableMap<Double, ResourceKey<LootTable>> lootWeights = new TreeMap<>();
    private static double totalWeight = 0;
    private static boolean isInitialized = false;

    public synchronized void initialize() {
        if (isInitialized) return;

        SkyGridConfig.LootConfig config = SkyGridConfigManager.get().loot;
        Set<String> bannedLoot = new HashSet<>(config.bannedLootTables);

        // Convert configured string keys into ResourceKeys
        Map<ResourceKey<LootTable>, Double> customWeights = new HashMap<>();
        for (Map.Entry<String, Double> entry : config.lootTableWeights.entrySet()) {
            Identifier id = Identifier.parse(entry.getKey());
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, id);
            customWeights.put(key, entry.getValue());
        }

        // Discover known loot tables and count unconfigured tables
        List<ResourceKey<LootTable>> validLootTables = new ArrayList<>();
        int unconfiguredCount = 0;

        for (ResourceKey<LootTable> key : BuiltInLootTables.all()) {
            String locationString = key.identifier().toString();
            if (bannedLoot.contains(locationString)) {
                continue;
            }
            validLootTables.add(key);

            if (!customWeights.containsKey(key)) {
                unconfiguredCount++;
            }
        }

        // Normalize default weight share for unconfigured loot tables
        double effectiveDefaultWeight = (unconfiguredCount > 0 && config.defaultWeight > 0)
                ? config.defaultWeight / unconfiguredCount
                : 0.0;

        // Build weighted pool
        for (ResourceKey<LootTable> key : validLootTables) {
            double weight = customWeights.getOrDefault(key, effectiveDefaultWeight);
            if (weight > 0) {
                totalWeight += weight;
                lootWeights.put(totalWeight, key);
            }
        }

        isInitialized = true;
    }

    public static void reset() {
        isInitialized = false;
        lootWeights.clear();
        totalWeight = 0;
    }

    public ResourceKey<LootTable> getRandomLootTable(RandomSource random) {
        if (!isInitialized) initialize();
        if (lootWeights.isEmpty()) return BuiltInLootTables.SIMPLE_DUNGEON;

        double value = random.nextDouble() * totalWeight;
        var entry = lootWeights.higherEntry(value);
        return entry != null ? entry.getValue() : lootWeights.lastEntry().getValue();
    }
}