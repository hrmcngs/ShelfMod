package io.github.hrmcngs.shelfmod.client;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.init.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ShelfMod.MODID, value = Dist.CLIENT)
public final class ShelfModClientEvents {
    private ShelfModClientEvents() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(PoleScaffoldClientFx.INSTANCE,
                ModBlocks.BAMBOO_SCAFFOLD.get(),
                ModBlocks.KAO_BAMBOO_SCAFFOLD.get(),
                ModBlocks.IRON_SCAFFOLD.get());
    }
}
