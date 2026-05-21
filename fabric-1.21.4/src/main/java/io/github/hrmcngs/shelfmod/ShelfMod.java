package io.github.hrmcngs.shelfmod;

import io.github.hrmcngs.shelfmod.init.MeshColor;
import io.github.hrmcngs.shelfmod.init.ModBlocks;
import io.github.hrmcngs.shelfmod.init.ModItemGroups;
import io.github.hrmcngs.shelfmod.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShelfMod implements ModInitializer {
    public static final String MOD_ID = "shelfmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModItems.init();
        ModItemGroups.init();

        FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
        for (MeshColor c : MeshColor.values()) {
            fire.add(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c), 30, 60);
        }

        LOGGER.info("ShelfMod initialized");
    }
}
