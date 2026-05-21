package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.block.BraceScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.PoleScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.SafetyMeshPanelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ShelfMod.MODID);

    public static final DeferredBlock<PoleScaffoldBlock> BAMBOO_SCAFFOLD = BLOCKS.registerBlock(
            "bamboo_scaffold",
            poleFactory(1.5),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.5F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape());

    public static final DeferredBlock<PoleScaffoldBlock> KAO_BAMBOO_SCAFFOLD = BLOCKS.registerBlock(
            "kao_bamboo_scaffold",
            poleFactory(1.0),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.4F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape());

    public static final DeferredBlock<BraceScaffoldBlock> BAMBOO_BRACE = BLOCKS.registerBlock(
            "bamboo_brace",
            BraceScaffoldBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.4F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape());

    public static final DeferredBlock<PoleScaffoldBlock> IRON_SCAFFOLD = BLOCKS.registerBlock(
            "iron_scaffold",
            poleFactory(1.5),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .dynamicShape()
                    .requiresCorrectToolForDrops());

    public static final Map<MeshColor, DeferredBlock<SafetyMeshPanelBlock>> SAFETY_MESH = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredBlock<SafetyMeshPanelBlock>> SAFETY_MESH_FLAMMABLE = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredBlock<SafetyMeshPanelBlock>> SAFETY_MESH_HEATPROOF = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredBlock<SafetyMeshPanelBlock>> SAFETY_MESH_FLAMMABLE_HEATPROOF = new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, BLOCKS.registerBlock(
                    c.id() + "_safety_mesh",
                    meshFactory(false, false), meshProps(c, false)));
            SAFETY_MESH_FLAMMABLE.put(c, BLOCKS.registerBlock(
                    c.id() + "_safety_mesh_flammable",
                    meshFactory(true, false), meshProps(c, false)));
            SAFETY_MESH_HEATPROOF.put(c, BLOCKS.registerBlock(
                    c.id() + "_safety_mesh_heatproof",
                    meshFactory(false, true), meshProps(c, true)));
            SAFETY_MESH_FLAMMABLE_HEATPROOF.put(c, BLOCKS.registerBlock(
                    c.id() + "_safety_mesh_flammable_heatproof",
                    meshFactory(true, true), meshProps(c, true)));
        }
    }

    private static Function<BlockBehaviour.Properties, PoleScaffoldBlock> poleFactory(double halfWidth) {
        return props -> new PoleScaffoldBlock(halfWidth, props);
    }

    private static Function<BlockBehaviour.Properties, SafetyMeshPanelBlock> meshFactory(boolean flammable, boolean heatproof) {
        return props -> new SafetyMeshPanelBlock(flammable, heatproof, props);
    }

    private static BlockBehaviour.Properties meshProps(MeshColor c, boolean heatproof) {
        float hardness = heatproof ? 0.4F : 0.2F;
        float resistance = heatproof ? 2.0F : 0.4F;
        return BlockBehaviour.Properties.of()
                .mapColor(c.mapColor())
                .strength(hardness, resistance)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .isViewBlocking((s, l, p) -> false)
                .isSuffocating((s, l, p) -> false);
    }

    private ModBlocks() {}
}
