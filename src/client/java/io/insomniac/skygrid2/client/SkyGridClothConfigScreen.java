package io.insomniac.skygrid2.client;

import io.insomniac.skygrid2.SkyGridConfig;
import io.insomniac.skygrid2.SkyGridConfigManager;
import io.insomniac.skygrid2.Skygrid2;
import io.insomniac.skygrid2.selectors.SkyGridBlockSelector;
import io.insomniac.skygrid2.selectors.SkyGridEntitySelector;
import io.insomniac.skygrid2.selectors.SkyGridLootTableSelector;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkyGridClothConfigScreen {

    public static Screen createScreen(Screen parent) {
        SkyGridConfig config = SkyGridConfigManager.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("SkyGrid II Configuration"));

        builder.setSavingRunnable(() -> {
            // Save changes to disk
            SkyGridConfigManager.save();
            // Force selectors to re-initialize
            SkyGridBlockSelector.reset();
            SkyGridEntitySelector.reset();
            SkyGridLootTableSelector.reset();
        });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory blocksCategory = builder.getOrCreateCategory(Component.literal("Blocks"));
        ConfigCategory spawnerEntitiesCategory = builder.getOrCreateCategory(Component.literal("Spawner Entities"));
        ConfigCategory lootTablesCategory = builder.getOrCreateCategory(Component.literal("Loot Tables"));

        // BLOCKS
        blocksCategory.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Default Block Weight"),
                        config.blocks.defaultWeight)
                .setDefaultValue(config.blocks.defaultWeight)
                .setSaveConsumer(newValue -> config.blocks.defaultWeight = newValue)
                .build());
        blocksCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Banned Blocks (ID & Tags)"),
                        config.blocks.bannedBlocks)
                .setDefaultValue(config.blocks.bannedBlocks)
                .setSaveConsumer(newList -> config.blocks.bannedBlocks = new ArrayList<>(newList))
                .setExpanded(false) // Optional: Keeps the list expanded by default for easy viewing
                .build());
        blocksCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Weighted Entries (tag=weight)"),
                        mapToStringList(config.blocks.weightedEntries))
                .setDefaultValue(mapToStringList(config.blocks.weightedEntries))
                .setSaveConsumer(newList -> {
                    // Converts the user's edited string list back into your Map
                    config.blocks.weightedEntries = stringListToMap(newList);
                })
                .setExpanded(true)
                .build());

        // ENTITIES
        spawnerEntitiesCategory.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Default Spawner Entity Weight"),
                        config.entities.defaultWeight)
                .setDefaultValue(config.entities.defaultWeight)
                .setSaveConsumer(newValue -> config.entities.defaultWeight = newValue)
                .build());
        spawnerEntitiesCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Banned Entities"),
                        config.entities.bannedEntities)
                .setDefaultValue(config.entities.bannedEntities)
                .setSaveConsumer(newList -> config.entities.bannedEntities = new ArrayList<>(newList))
                        .setExpanded(false)
                .build());
        spawnerEntitiesCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Weighted Entries (tag=weight)"),
                        mapToStringList(config.entities.spawnerEntityWeights))
                .setDefaultValue(mapToStringList(config.entities.spawnerEntityWeights))
                .setSaveConsumer(newList -> {
                    // Converts the user's edited string list back into your Map
                    config.entities.spawnerEntityWeights = stringListToMap(newList);
                })
                .setExpanded(true)
                .build());

        // LOOT TABLES
        lootTablesCategory.addEntry(entryBuilder.startDoubleField(
                        Component.literal("Default Loot Table Weight"),
                        config.loot.defaultWeight)
                .setDefaultValue(config.loot.defaultWeight)
                .setSaveConsumer(newValue -> config.loot.defaultWeight = newValue)
                .build());
        lootTablesCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Banned Loot Tables"),
                        config.loot.bannedLootTables)
                .setDefaultValue(config.loot.bannedLootTables)
                .setSaveConsumer(newList -> config.loot.bannedLootTables = new ArrayList<>(newList))
                        .setExpanded(false)
                .build());
        lootTablesCategory.addEntry(entryBuilder.startStrList(
                        Component.literal("Weighted Entries (tag=weight)"),
                        mapToStringList(config.loot.lootTableWeights))
                .setDefaultValue(mapToStringList(config.loot.lootTableWeights))
                .setSaveConsumer(newList -> {
                    // Converts the user's edited string list back into your Map
                    config.loot.lootTableWeights = stringListToMap(newList);
                })
                .setExpanded(true)
                .build());

        return builder.build();
    }

    // Convert Map to a List of "key=value" strings for Cloth Config
    public static List<String> mapToStringList(Map<String, Double> map) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            list.add(entry.getKey() + "=" + entry.getValue());
        }
        return list;
    }

    // Parse "key=value" strings back into Map for saving
    public static Map<String, Double> stringListToMap(List<String> list) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (String item : list) {
            String[] parts = item.split("=", 2);
            if (parts.length == 2) {
                try {
                    double weight = Double.parseDouble(parts[1].trim());
                    map.put(parts[0].trim(), weight);
                } catch (NumberFormatException e) {
                    Skygrid2.LOGGER.error("Malformed config: could not parse number.", e);
                }
            } else {
                Skygrid2.LOGGER.error("Malformed config: expected tag=value[float], recieved: {}", item);
            }
        }
        return map;
    }
}