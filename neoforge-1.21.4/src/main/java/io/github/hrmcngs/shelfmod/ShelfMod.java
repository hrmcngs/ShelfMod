package io.github.hrmcngs.shelfmod;

import io.github.hrmcngs.shelfmod.init.ModBlocks;
import io.github.hrmcngs.shelfmod.init.ModCreativeTabs;
import io.github.hrmcngs.shelfmod.init.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ShelfMod.MODID)
public class ShelfMod {
    public static final String MODID = "shelfmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShelfMod(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}
