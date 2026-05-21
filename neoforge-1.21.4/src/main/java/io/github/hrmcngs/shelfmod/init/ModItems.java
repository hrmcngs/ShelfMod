package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ShelfMod.MODID);

    public static final DeferredItem<BlockItem> BAMBOO_SCAFFOLD =
            ITEMS.registerSimpleBlockItem(ModBlocks.BAMBOO_SCAFFOLD);
    public static final DeferredItem<BlockItem> KAO_BAMBOO_SCAFFOLD =
            ITEMS.registerSimpleBlockItem(ModBlocks.KAO_BAMBOO_SCAFFOLD);
    public static final DeferredItem<BlockItem> BAMBOO_BRACE =
            ITEMS.registerSimpleBlockItem(ModBlocks.BAMBOO_BRACE);
    public static final DeferredItem<BlockItem> IRON_SCAFFOLD =
            ITEMS.registerSimpleBlockItem(ModBlocks.IRON_SCAFFOLD);

    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH_FLAMMABLE = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH_HEATPROOF = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH_FLAMMABLE_HEATPROOF = new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH.get(c)));
            SAFETY_MESH_FLAMMABLE.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c)));
            SAFETY_MESH_HEATPROOF.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH_HEATPROOF.get(c)));
            SAFETY_MESH_FLAMMABLE_HEATPROOF.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c)));
        }
    }

    private ModItems() {}
}
