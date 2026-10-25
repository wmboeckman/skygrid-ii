package io.insomniac.skygrid2;

import io.insomniac.skygrid2.selectors.DynamicWeightedSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class Util {

    /// Retrieves the associated BlockEnitity Identifier as a String from a BlockState and BlockPos
    public static String getBlockEntityId(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof EntityBlock entityBlock) {
            // Create a temporary block entity instance for the state
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
            if (blockEntity != null) {
                // Retrieve the registered ID directly from the BlockEntityType
                Identifier typeId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
                if (typeId != null) {
                    return typeId.toString();
                }
            }
        }
        return null;
    }

    /// Helper function to initialize a DynamicWeightedSelector for a given BuiltInRegistry
    public static <T> void initializeSelector(
            Registry<T> registry,
            List<String> bannedEntries,
            Map<String, Double> weightedEntries,
            double defaultWeightConfig,
            BiPredicate<T, Identifier> hardcodedBanChecker,
            Function<T, Object> resultMapper,
            DynamicWeightedSelector<Object> selector
    ) {
        // NOTE: likely inefficient due to multiple repeated loops, but is only done once so idrc.

        // Parse banned entries & tags
        Set<T> bannedSet = new HashSet<>();
        for (String entry : bannedEntries) {
            if (entry.startsWith("#")) {
                TagKey<T> tagKey = TagKey.create(registry.key(), Identifier.parse(entry.substring(1)));
                for (Holder<T> holder : registry.getTagOrEmpty(tagKey)) {
                    bannedSet.add(holder.value());
                }
            } else {
                registry.getOptional(Identifier.parse(entry)).ifPresent(bannedSet::add);
            }
        }

        // Parse custom configured weights & tags
        Map<T, Double> customWeights = new HashMap<>();
        for (Map.Entry<String, Double> entry : weightedEntries.entrySet()) {
            String key = entry.getKey();
            double weight = entry.getValue();

            if (key.startsWith("#")) {
                TagKey<T> tagKey = TagKey.create(registry.key(), Identifier.parse(key.substring(1)));
                for (Holder<T> holder : registry.getTagOrEmpty(tagKey)) {
                    customWeights.put(holder.value(), weight);
                }
            } else {
                registry.getOptional(Identifier.parse(key)).ifPresent(item -> {
                    customWeights.put(item, weight);
                });
            }
        }

        // First pass: Collect valid items and count unconfigured entries
        List<T> validItems = new ArrayList<>();
        int unconfiguredCount = 0;

        for (T item : registry) {
            Identifier id = registry.getKey(item);
            if (bannedSet.contains(item) || (id != null && hardcodedBanChecker.test(item, id))) {
                continue;
            }
            validItems.add(item);

            if (!customWeights.containsKey(item)) {
                unconfiguredCount++;
            }
        }

        // Calculate effective per-item weight for default entries
        double effectiveDefaultWeight = (unconfiguredCount > 0 && defaultWeightConfig > 0)
                ? defaultWeightConfig / unconfiguredCount
                : 0.0;

        // Second pass: Populate weighted selector
        for (T item : validItems) {
            double weight = customWeights.getOrDefault(item, effectiveDefaultWeight);
            if (weight > 0) {
                selector.add(weight, resultMapper.apply(item));
            }
        }
    }
}
