package io.insomniac.skygrid2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SkyGridConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("skygrid2.json")
            .toFile();

    private static SkyGridConfig INSTANCE;

    public static SkyGridConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, SkyGridConfig.class);
            } catch (IOException e) {
                Skygrid2.LOGGER.error("Failed to load config file! Falling back to defaults.", e);
                INSTANCE = createDefaults();
            }
        } else {
            INSTANCE = createDefaults();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Skygrid2.LOGGER.error("Failed to save config file!", e);
        }
    }

    private static SkyGridConfig createDefaults() {
        SkyGridConfig config = new SkyGridConfig();

        // ==========================================
        //             BLOCK CONFIG
        // ==========================================
        config.blocks.defaultWeight = 10.0;
        // Ban game-breaking or broken blocks
        config.blocks.bannedBlocks.addAll(List.of(
                "#minecraft:air",
                "minecraft:cave_air",
                "minecraft:void_air",
                "minecraft:bedrock",
                "minecraft:barrier",
                "minecraft:command_block",
                "minecraft:chain_command_block",
                "minecraft:repeating_command_block",
                "minecraft:structure_block",
                "minecraft:structure_void",
                "minecraft:jigsaw",
                "minecraft:end_portal",
                "minecraft:end_portal_frame",
                "minecraft:end_gateway",
                "minecraft:nether_portal",
                "minecraft:vault",
                "minecraft:trial_spawner",
                "#minecraft:beds",
                "#minecraft:doors"
        ));
        // Weighted blocks (Overworld)
        config.blocks.weightedEntries.put("#minecraft:base_stone_overworld", 25.0);
        config.blocks.weightedEntries.put("#minecraft:dirt", 15.0);
        config.blocks.weightedEntries.put("minecraft:cobblestone", 10.0);
        config.blocks.weightedEntries.put("#minecraft:logs", 8.0);
        config.blocks.weightedEntries.put("#minecraft:leaves", 6.0);
        config.blocks.weightedEntries.put("#minecraft:sand", 4.0);
        config.blocks.weightedEntries.put("minecraft:gravel", 4.0);
        config.blocks.weightedEntries.put("minecraft:water", 3.0);
        config.blocks.weightedEntries.put("minecraft:lava", 1.5);
        // Ores
        config.blocks.weightedEntries.put("minecraft:coal_ore", 3.0);
        config.blocks.weightedEntries.put("#minecraft:coal_ores", 3.0);
        config.blocks.weightedEntries.put("#minecraft:iron_ores", 2.0);
        config.blocks.weightedEntries.put("#minecraft:copper_ores", 2.5);
        config.blocks.weightedEntries.put("minecraft:redstone_ore", 1.5);
        config.blocks.weightedEntries.put("minecraft:lapis_ore", 1.0);
        config.blocks.weightedEntries.put("#minecraft:gold_ores", 2.0);
        config.blocks.weightedEntries.put("minecraft:diamond_ore", 0.3);
        config.blocks.weightedEntries.put("minecraft:emerald_ore", 0.3);
        config.blocks.weightedEntries.put("minecraft:ancient_debris", 0.1);
        // Utility blocks
        config.blocks.weightedEntries.put("minecraft:chest", 2.0);
        config.blocks.weightedEntries.put("minecraft:crafting_table", 0.5);
        config.blocks.weightedEntries.put("minecraft:furnace", 0.5);
        config.blocks.weightedEntries.put("minecraft:spawner", 1.5);


        // ==========================================
        //             SPAWNER CONFIG
        // ==========================================
        config.entities.defaultWeight = 4.0;

        config.entities.bannedEntities.addAll(List.of(
                "minecraft:warden",
                "minecraft:ender_dragon",
                "minecraft:wither",
                "minecraft:elder_guardian"
        ));

        config.entities.spawnerEntityWeights.put("minecraft:zombie", 12.0);
        config.entities.spawnerEntityWeights.put("minecraft:skeleton", 10.0);
        config.entities.spawnerEntityWeights.put("minecraft:pig", 8.0);
        config.entities.spawnerEntityWeights.put("minecraft:cow", 8.0);
        config.entities.spawnerEntityWeights.put("minecraft:sheep", 6.0);
        config.entities.spawnerEntityWeights.put("minecraft:chicken", 6.0);
        config.entities.spawnerEntityWeights.put("minecraft:spider", 6.0);
        config.entities.spawnerEntityWeights.put("minecraft:creeper", 6.0);
        config.entities.spawnerEntityWeights.put("minecraft:blaze", 5.0);


        // ==========================================
        //              LOOT CONFIG
        // ==========================================
        config.loot.defaultWeight = 2.0;

        config.loot.lootTableWeights.put("minecraft:chests/simple_dungeon", 20.0);
        config.loot.lootTableWeights.put("minecraft:chests/abandoned_mineshaft", 18.0);
        config.loot.lootTableWeights.put("minecraft:chests/village/village_plains_house", 16.0);
        config.loot.lootTableWeights.put("minecraft:chests/desert_pyramid", 15.0);
        config.loot.lootTableWeights.put("minecraft:chests/jungle_temple", 15.0);
        config.loot.lootTableWeights.put("minecraft:chests/ruined_portal", 10.0);
        config.loot.lootTableWeights.put("minecraft:chests/end_city_treasure", 5.0);
        config.loot.lootTableWeights.put("minecraft:chests/ancient_city", 2.0);
        config.loot.lootTableWeights.put("minecraft:chests/stronghold_library", 2.0);
        config.loot.lootTableWeights.put("minecraft:chests/bastion_treasure", 1.0);


        return config;
    }
}
