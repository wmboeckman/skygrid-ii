package io.insomniac.skygrid2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkyGridConfig {
    public BlockConfig blocks = new BlockConfig();
    public EntityConfig entities = new EntityConfig();
    public LootConfig loot = new LootConfig();

    public static class BlockConfig {
        public double defaultWeight = 8.0;
        public List<String> bannedBlocks = new ArrayList<>();
        public Map<String, Double> weightedEntries = new LinkedHashMap<>();
    }

    public static class EntityConfig {
        public double defaultWeight = 8.0;
        public List<String> bannedEntities = new ArrayList<>();
        public Map<String, Double> spawnerEntityWeights = new LinkedHashMap<>();
    }

    public static class LootConfig {
        public double defaultWeight = 8.0;
        public List<String> bannedLootTables = new ArrayList<>();
        public Map<String, Double> lootTableWeights = new LinkedHashMap<>();
    }
}
