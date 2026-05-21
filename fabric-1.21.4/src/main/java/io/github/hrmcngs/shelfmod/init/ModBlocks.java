package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.ScaffoldingBlock;
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
            ScaffoldingBlock::new,
            AbstractBlock.Settings.copy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.YELLOW)
                    .sounds(BlockSoundGroup.BAMBOO));

    public static final Block IRON_SCAFFOLD = register(
            "iron_scaffold",
            ScaffoldingBlock::new,
            AbstractBlock.Settings.copy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.IRON_GRAY)
                    .sounds(BlockSoundGroup.METAL)
                    .strength(1.5F, 6.0F));

    public static final Map<MeshColor, Block> SAFETY_MESH = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Block> SAFETY_MESH_FLAMMABLE = new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, register(
                    c.id() + "_safety_mesh", Block::new, meshSettings(c)));
            SAFETY_MESH_FLAMMABLE.put(c, register(
                    c.id() + "_safety_mesh_flammable", Block::new, meshSettings(c)));
        }
    }

    private static AbstractBlock.Settings meshSettings(MeshColor c) {
        return AbstractBlock.Settings.create()
                .mapColor(c.mapColor())
                .strength(0.2F)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque()
                .blockVision((s, w, p) -> false)
                .suffocates((s, w, p) -> false);
    }

    private static Block register(String name,
                                  Function<AbstractBlock.Settings, Block> factory,
                                  AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(ShelfMod.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        Block block = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static void init() {}

    private ModBlocks() {}
}
