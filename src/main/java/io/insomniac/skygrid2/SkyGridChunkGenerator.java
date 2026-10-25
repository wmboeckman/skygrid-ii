package io.insomniac.skygrid2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.insomniac.skygrid2.selectors.SkyGridBlockSelector;
import io.insomniac.skygrid2.selectors.SkyGridEntitySelector;
import io.insomniac.skygrid2.selectors.SkyGridLootTableSelector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkyGridChunkGenerator extends ChunkGenerator {

    private final SkyGridBlockSelector blockSelector = new SkyGridBlockSelector();
    private final SkyGridEntitySelector entitySelector = new SkyGridEntitySelector();
    private final SkyGridLootTableSelector lootTableSelector = new SkyGridLootTableSelector();

    // TODO: pull from a config file!
    private static final int SPACING = 4; // SkyGrid typically has 3 blocks of air between each block
    private static final boolean PLACE_DIRT_BELOW_ATTACHED = true;

    // Codec read/writes the BiomeSource using a RecordCodecBuilder
    public static final MapCodec<SkyGridChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, instance.stable(SkyGridChunkGenerator::new))
    );

    public SkyGridChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {

    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState randomState, ChunkAccess protoChunk) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return heightAccessor.getMaxY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        // Return a NoiseColumn starting at the bottom of the world, with an empty array of blocks.
        // The NoiseColumn class automatically returns AIR for any Y level that isn't provided in the array.
        return new NoiseColumn(heightAccessor.getMinY(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {

    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess centerChunk) {

        RandomSource randomSource = RandomSource.create(centerChunk.getPos().pack());

        int chunkStartX = centerChunk.getPos().getMinBlockX();
        int chunkStartZ = centerChunk.getPos().getMinBlockZ();

        int bottomY = centerChunk.getMinY();
        int topY = centerChunk.getMaxY(); // Or cap it at a specific height like 128

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = bottomY; y < topY; y++) {
                    int worldX = chunkStartX + x;
                    int worldZ = chunkStartZ + z;
                    if (Math.floorMod(worldX, SPACING) == 0 && Math.floorMod(worldZ, SPACING) == 0 && Math.floorMod(y, SPACING) == 0) {
                        mutablePos.set(worldX, y, worldZ);
                        fillBlock(centerChunk, mutablePos, randomSource);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(centerChunk);
    }

    private void fillBlock(ChunkAccess chunk, BlockPos.MutableBlockPos position, RandomSource random) {
        BlockState selectedBlock = blockSelector.getRandomBlock(random);

        // BLOCK ATTACHMENT LOGIC
        // TODO: make this tile-dependent (hanging, side, top, etc.)
        if (SkyGridBlockSelector.isAttachedOrDependentBlock(selectedBlock)) {
            if (PLACE_DIRT_BELOW_ATTACHED) {
                BlockPos abovePos = position.above();
                // Check depth boundaries
                chunk.setBlockState(position, Blocks.DIRT.defaultBlockState(), 0);
                if (abovePos.getY() <= chunk.getMaxY()) {
                    chunk.setBlockState(abovePos, selectedBlock, 0);
                }
            } else {
                chunk.setBlockState(position, selectedBlock, 0);
            }
        } else {
            chunk.setBlockState(position, selectedBlock, 0);
        }

        // SPAWNER LOGIC
        if (selectedBlock.is(Blocks.SPAWNER)) {
            CompoundTag blockEntityTag = new CompoundTag();
            // Store required block nbt data
            blockEntityTag.putString("id", "minecraft:mob_spawner");
            blockEntityTag.putInt("x", position.getX());
            blockEntityTag.putInt("y", position.getY());
            blockEntityTag.putInt("z", position.getZ());
            // Create spawner nbt tags
            EntityType<?> entity = entitySelector.getRandomEntity(random);
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity).toString();
            CompoundTag spawnDataTag = new CompoundTag();
            CompoundTag entityTag = new CompoundTag();
            entityTag.putString("id", entityId);
            // Store spawner entity + data
            spawnDataTag.put("entity", entityTag);
            blockEntityTag.put("SpawnData", spawnDataTag);
            chunk.setBlockEntityNbt(blockEntityTag);
        }

        // LOOT CHEST LOGIC
        else if (SkyGridBlockSelector.isLootContainer(selectedBlock)) {
            String blockEntityId = Util.getBlockEntityId(selectedBlock, position);
            if (blockEntityId != null) {
                ResourceKey<LootTable> lootTableKey = lootTableSelector.getRandomLootTable(random);
                // Resolve the entity identifier dynamically (e.g., "minecraft:shulker_box", "minecraft:barrel", "minecraft:chest")
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(selectedBlock.getBlock());
                // Store required block nbt data
                CompoundTag blockEntityTag = new CompoundTag();
                blockEntityTag.putString("id", blockEntityId);
                blockEntityTag.putInt("x", position.getX());
                blockEntityTag.putInt("y", position.getY());
                blockEntityTag.putInt("z", position.getZ());
                // Store loot table id & seed
                blockEntityTag.putString("LootTable", lootTableKey.identifier().getPath());
                blockEntityTag.putLong("LootTableSeed", random.nextLong());
                chunk.setBlockEntityNbt(blockEntityTag);
            }
        }
    }
}