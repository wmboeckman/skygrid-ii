package io.insomniac.skygrid2.selectors;

import io.insomniac.skygrid2.SkyGridConfig;
import io.insomniac.skygrid2.SkyGridConfigManager;
import io.insomniac.skygrid2.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;

import java.util.*;


public class SkyGridEntitySelector {
    private static final DynamicWeightedSelector<EntityType<?>> selector = new DynamicWeightedSelector<>();
    private static volatile boolean isInitialized = false;

    @SuppressWarnings("unchecked")
    public synchronized void initialize() {
        if (isInitialized) return;

        SkyGridConfig.EntityConfig config = SkyGridConfigManager.get().entities;

        Util.initializeSelector(
                BuiltInRegistries.ENTITY_TYPE,
                config.bannedEntities,
                config.spawnerEntityWeights,
                config.defaultWeight,
                (entityType, id) -> entityType == EntityTypes.PLAYER ||
                        entityType == EntityTypes.MARKER ||
                        entityType == EntityTypes.ITEM ||
                        entityType.getCategory() == MobCategory.MISC,
                entityType -> entityType,
                (DynamicWeightedSelector<Object>) (Object) selector
        );

        isInitialized = true;
    }

    public static void reset() {
        isInitialized = false;
    }

    public EntityType<?> getRandomEntity(RandomSource random) {
        if (!isInitialized) initialize();
        return selector.getRandom(random, EntityTypes.ZOMBIE);
    }
}