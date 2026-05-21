package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.block.BraceScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.PoleScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.SafetyMeshPanelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

/**
 * Block roster modelled on Hong Kong double-row bamboo scaffolding practice:
 *
 *   - {@code bamboo_scaffold}     = 毛竹 Mao Jue (~3 px) — load-bearing verticals
 *   - {@code kao_bamboo_scaffold} = 篙竹 Kao Jue (~2 px) — ledgers / horizontals
 *   - {@code bamboo_brace}        = 斜撑 — diagonal brace (Kao thickness)
 *   - {@code iron_scaffold}       = modern metal alternative
 *   - {@code *_safety_mesh*}      = mesh in four grades (flammable × heatproof matrix)
 */
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ShelfMod.MODID);

    public static final RegistryObject<Block> BAMBOO_SCAFFOLD = BLOCKS.register("bamboo_scaffold",
            () -> new PoleScaffoldBlock(1.5, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.5F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape()));

    public static final RegistryObject<Block> KAO_BAMBOO_SCAFFOLD = BLOCKS.register("kao_bamboo_scaffold",
            () -> new PoleScaffoldBlock(1.0, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.4F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape()));

    public static final RegistryObject<Block> BAMBOO_BRACE = BLOCKS.register("bamboo_brace",
            () -> new BraceScaffoldBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(0.4F)
                    .sound(SoundType.BAMBOO)
                    .noOcclusion()
                    .dynamicShape()));

    public static final RegistryObject<Block> IRON_SCAFFOLD = BLOCKS.register("iron_scaffold",
            () -> new PoleScaffoldBlock(1.5, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .dynamicShape()
                    .requiresCorrectToolForDrops()));

    // Mesh: 4 grades per colour (flammable × heatproof matrix).
    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH_HEATPROOF =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH_FLAMMABLE_HEATPROOF =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, BLOCKS.register(c.id() + "_safety_mesh",
                    () -> new SafetyMeshPanelBlock(false, false, meshProps(c, false))));
            SAFETY_MESH_FLAMMABLE.put(c, BLOCKS.register(c.id() + "_safety_mesh_flammable",
                    () -> new SafetyMeshPanelBlock(true, false, meshProps(c, false))));
            SAFETY_MESH_HEATPROOF.put(c, BLOCKS.register(c.id() + "_safety_mesh_heatproof",
                    () -> new SafetyMeshPanelBlock(false, true, meshProps(c, true))));
            SAFETY_MESH_FLAMMABLE_HEATPROOF.put(c, BLOCKS.register(c.id() + "_safety_mesh_flammable_heatproof",
                    () -> new SafetyMeshPanelBlock(true, true, meshProps(c, true))));
        }
    }

    private static BlockBehaviour.Properties meshProps(MeshColor c, boolean heatproof) {
        // Heatproof mesh is slightly tougher to break and resists explosions better.
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
