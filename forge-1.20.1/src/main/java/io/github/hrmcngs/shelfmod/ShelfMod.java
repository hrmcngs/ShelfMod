package io.github.hrmcngs.shelfmod;

import io.github.hrmcngs.shelfmod.init.MeshColor;
import io.github.hrmcngs.shelfmod.init.ModBlocks;
import io.github.hrmcngs.shelfmod.init.ModCreativeTabs;
import io.github.hrmcngs.shelfmod.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShelfMod.MODID)
public class ShelfMod {
    public static final String MODID = "shelfmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ShelfMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);

        bus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BAMBOO_SCAFFOLD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.KAO_BAMBOO_SCAFFOLD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.BAMBOO_BRACE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.IRON_SCAFFOLD.get(), RenderType.cutout());
        for (MeshColor c : MeshColor.values()) {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAFETY_MESH.get(c).get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c).get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAFETY_MESH_HEATPROOF.get(c).get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c).get(), RenderType.cutoutMipped());
        }
    }
}
