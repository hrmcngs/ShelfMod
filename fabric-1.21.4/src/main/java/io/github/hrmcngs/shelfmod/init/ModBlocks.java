package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.block.BraceScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.PoleScaffoldBlock;
import io.github.hrmcngs.shelfmod.block.SafetyMeshPanelBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {

    public static final Block BAMBOO_SCAFFOLD = register(
            "bamboo_scaffold",
            s -> new PoleScaffoldBlock(1.5, s),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.YELLOW)
                    .strength(0.5F)
                    .sounds(BlockSoundGroup.BAMBOO)
                    .nonOpaque()
                    .dynamicBounds());

    public static final Block KAO_BAMBOO_SCAFFOLD = register(
            "kao_bamboo_scaffold",
            s -> new PoleScaffoldBlock(1.0, s),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.YELLOW)
                    .strength(0.4F)
                    .sounds(BlockSoundGroup.BAMBOO)
                    .nonOpaque()
                    .dynamicBounds());

    public static final Block BAMBOO_BRACE = register(
            "bamboo_brace",
            BraceScaffoldBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.YELLOW)
                    .strength(0.4F)
                    .sounds(BlockSoundGroup.BAMBOO)
                    .nonOpaque()
                    .dynamicBounds());

    public static final Block IRON_SCAFFOLD = register(
            "iron_scaffold",
            s -> new PoleScaffoldBlock(1.5, s),
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .strength(1.5F, 6.0F)
                    .sounds(BlockSoundGroup.METAL)
                    .nonOpaque()
                    .dynamicBounds()
                    .requiresTool());

    public static final Map<MeshColor, Block> SAFETY_MESH = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Block> SAFETY_MESH_FLAMMABLE = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Block> SAFETY_MESH_HEATPROOF = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Block> SAFETY_MESH_FLAMMABLE_HEATPROOF = new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, register(
                    c.id() + "_safety_mesh",
                    s -> new SafetyMeshPanelBlock(false, false, s), meshSettings(c, false)));
            SAFETY_MESH_FLAMMABLE.put(c, register(
                    c.id() + "_safety_mesh_flammable",
                    s -> new SafetyMeshPanelBlock(true, false, s), meshSettings(c, false)));
            SAFETY_MESH_HEATPROOF.put(c, register(
                    c.id() + "_safety_mesh_heatproof",
                    s -> new SafetyMeshPanelBlock(false, true, s), meshSettings(c, true)));
            SAFETY_MESH_FLAMMABLE_HEATPROOF.put(c, register(
                    c.id() + "_safety_mesh_flammable_heatproof",
                    s -> new SafetyMeshPanelBlock(true, true, s), meshSettings(c, true)));
        }
    }

    private static AbstractBlock.Settings meshSettings(MeshColor c, boolean heatproof) {
        float hardness = heatproof ? 0.4F : 0.2F;
        float resistance = heatproof ? 2.0F : 0.4F;
        return AbstractBlock.Settings.create()
                .mapColor(c.mapColor())
                .strength(hardness, resistance)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque()
                .blockVision((s, w, p) -> false)
                .suffocates((s, w, p) -> false);
    }

    private static <T extends Block> T register(String name,
                                                Function<AbstractBlock.Settings, T> factory,
                                                AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(ShelfMod.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        T block = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static void init() {}

    private ModBlocks() {}
}
