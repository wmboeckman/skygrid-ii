package io.insomniac.skygrid2.selectors;

import io.insomniac.skygrid2.SkyGridConfig;
import io.insomniac.skygrid2.SkyGridConfigManager;
import io.insomniac.skygrid2.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.*;

import java.util.*;


public class SkyGridBlockSelector {
    private static final DynamicWeightedSelector<BlockState> selector = new DynamicWeightedSelector<>();
    private static volatile boolean isInitialized = false;

    @SuppressWarnings("unchecked")
    public synchronized void initialize() {
        if (isInitialized) return;

        SkyGridConfig.BlockConfig config = SkyGridConfigManager.get().blocks;

        Util.initializeSelector(
                BuiltInRegistries.BLOCK,
                config.bannedBlocks,
                config.weightedEntries,
                config.defaultWeight,
                (block, id) -> {
                    BlockState state = block.defaultBlockState();
                    return state.isAir() || block.defaultDestroyTime() < 0.0f ||
                            (block.asItem() == Items.AIR && !(block instanceof LiquidBlock)) ||
                            state.is(Blocks.VAULT) || state.is(Blocks.TRIAL_SPAWNER) ||
                            state.is(BlockTags.BEDS) || state.is(BlockTags.DOORS);
                },
                Block::defaultBlockState,
                (DynamicWeightedSelector<Object>) (Object) selector
        );

        isInitialized = true;
    }

    public static void reset() {
        isInitialized = false;
    }

    public BlockState getRandomBlock(RandomSource random) {
        if (!isInitialized) initialize();
        return selector.getRandom(random, Blocks.DIRT.defaultBlockState());
    }

    // TODO: more customizability planned
    public static boolean isAttachedOrDependentBlock(BlockState state) {
        Block block = state.getBlock();

        // 1. Torches, Lanterns, Banners, Signs
        if (block instanceof TorchBlock ||
                block instanceof WallTorchBlock ||
                block instanceof LanternBlock ||
                block instanceof SignBlock ||
                block instanceof WallSignBlock) {
            return true;
        }

        // 2. Plants, Flowers, Saplings, Crops, Sugar Cane
        if (block instanceof BushBlock ||
                block instanceof GrowingPlantBlock ||
                block instanceof CactusBlock ||
                block instanceof SugarCaneBlock ||
                state.is(BlockTags.CROPS) ||
                state.is(BlockTags.FLOWERS)) {
            return true;
        }

        // 3. Redstone components, Rails, Pressure Plates, Buttons
        if (block instanceof RedStoneWireBlock ||
                block instanceof DiodeBlock || // Repeaters, Comparators
                block instanceof ButtonBlock ||
                block instanceof PressurePlateBlock ||
                block instanceof BaseRailBlock ||
                block instanceof LeverBlock) {
            return true;
        }

        // 4. Attachments & Miscellaneous (Tripwire, Chains, Ladders)
        if (block instanceof LadderBlock ||
                block instanceof TripWireBlock ||
                block instanceof TripWireHookBlock) {
            return true;
        }

        return false;
    }

    public static boolean isLootContainer(BlockState state) {
        return state.is(Blocks.CHEST) ||
                state.is(Blocks.TRAPPED_CHEST) ||
                state.is(Blocks.BARREL) ||
                state.is(BlockTags.SHULKER_BOXES) ||
                // modded chest support (e.g. Copper Chests, Tech Reborn, IronChests)
                // TODO: check for BlockEntity with an inventory first!
                state.getBlock().getDescriptionId().contains("chest");
    }
}