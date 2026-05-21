package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.block.FlammableMeshBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ShelfMod.MODID);

    public static final DeferredBlock<ScaffoldingBlock> BAMBOO_SCAFFOLD = BLOCKS.registerBlock(
            "bamboo_scaffold",
            ScaffoldingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .sound(SoundType.BAMBOO));

    public static final DeferredBlock<ScaffoldingBlock> IRON_SCAFFOLD = BLOCKS.registerBlock(
            "iron_scaffold",
            ScaffoldingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(1.5F, 6.0F));

    public static final Map<MeshColor, DeferredBlock<Block>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredBlock<FlammableMeshBlock>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, BLOCKS.registerSimpleBlock(
                    c.id() + "_safety_mesh", meshProps(c)));
            SAFETY_MESH_FLAMMABLE.put(c, BLOCKS.registerBlock(
                    c.id() + "_safety_mesh_flammable",
                    FlammableMeshBlock::new,
                    meshProps(c)));
        }
    }

    private static BlockBehaviour.Properties meshProps(MeshColor c) {
        return BlockBehaviour.Properties.of()
                .mapColor(c.mapColor())
                .strength(0.2F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .isViewBlocking((s, l, p) -> false)
                .isSuffocating((s, l, p) -> false);
    }

    private ModBlocks() {}
}
