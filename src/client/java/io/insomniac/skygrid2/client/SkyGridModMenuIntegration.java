package io.insomniac.skygrid2.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkyGridModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // This links the Mod Menu button to your custom Cloth Config screen builder
        return parent -> SkyGridClothConfigScreen.createScreen(parent);
    }
}
