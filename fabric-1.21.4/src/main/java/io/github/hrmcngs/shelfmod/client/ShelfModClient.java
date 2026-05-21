package io.github.hrmcngs.shelfmod.client;

import io.github.hrmcngs.shelfmod.init.MeshColor;
import io.github.hrmcngs.shelfmod.init.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class ShelfModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap m = BlockRenderLayerMap.INSTANCE;

        m.putBlock(ModBlocks.BAMBOO_SCAFFOLD,     RenderLayer.getCutout());
        m.putBlock(ModBlocks.KAO_BAMBOO_SCAFFOLD, RenderLayer.getCutout());
        m.putBlock(ModBlocks.BAMBOO_BRACE,        RenderLayer.getCutout());
        m.putBlock(ModBlocks.IRON_SCAFFOLD,       RenderLayer.getCutout());
        for (MeshColor c : MeshColor.values()) {
            m.putBlock(ModBlocks.SAFETY_MESH.get(c),                     RenderLayer.getCutoutMipped());
            m.putBlock(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c),           RenderLayer.getCutoutMipped());
            m.putBlock(ModBlocks.SAFETY_MESH_HEATPROOF.get(c),           RenderLayer.getCutoutMipped());
            m.putBlock(ModBlocks.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c), RenderLayer.getCutoutMipped());
        }
    }
}
