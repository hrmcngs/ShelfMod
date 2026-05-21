package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import io.github.hrmcngs.shelfmod.block.FlammableMeshBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ShelfMod.MODID);

    public static final RegistryObject<Block> BAMBOO_SCAFFOLD = BLOCKS.register("bamboo_scaffold",
            () -> new ScaffoldingBlock(BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .sound(SoundType.BAMBOO)));

    public static final RegistryObject<Block> IRON_SCAFFOLD = BLOCKS.register("iron_scaffold",
            () -> new ScaffoldingBlock(BlockBehaviour.Properties.copy(Blocks.SCAFFOLDING)
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(1.5F, 6.0F)));

    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Block>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, BLOCKS.register(c.id() + "_safety_mesh",
                    () -> new Block(meshProps(c))));
            SAFETY_MESH_FLAMMABLE.put(c, BLOCKS.register(c.id() + "_safety_mesh_flammable",
                    () -> new FlammableMeshBlock(meshProps(c))));
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
