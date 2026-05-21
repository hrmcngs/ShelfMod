package io.github.hrmcngs.shelfmod.client;

import io.github.hrmcngs.shelfmod.init.MeshColor;
import io.github.hrmcngs.shelfmod.init.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class ShelfModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BAMBOO_SCAFFOLD, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.IRON_SCAFFOLD,   RenderLayer.getCutout());
        for (MeshColor c : MeshColor.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SAFETY_MESH.get(c),           RenderLayer.getCutoutMipped());
            BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c), RenderLayer.getCutoutMipped());
        }
    }
}
