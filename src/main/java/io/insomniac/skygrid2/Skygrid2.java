package io.insomniac.skygrid2;

import io.insomniac.skygrid2.selectors.SkyGridBlockSelector;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Skygrid2 implements ModInitializer {

    public static final String ID = "skygrid2";

    public static final Logger LOGGER = LoggerFactory.getLogger(ID);

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath(ID, "skygrid"), SkyGridChunkGenerator.CODEC);
        SkyGridConfigManager.load();
    }
}
