package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ScaffoldingBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ShelfMod.MODID);

    public static final DeferredItem<ScaffoldingBlockItem> BAMBOO_SCAFFOLD = ITEMS.registerItem(
            "bamboo_scaffold",
            props -> new ScaffoldingBlockItem(ModBlocks.BAMBOO_SCAFFOLD.get(), props),
            new Item.Properties());

    public static final DeferredItem<ScaffoldingBlockItem> IRON_SCAFFOLD = ITEMS.registerItem(
            "iron_scaffold",
            props -> new ScaffoldingBlockItem(ModBlocks.IRON_SCAFFOLD.get(), props),
            new Item.Properties());

    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, DeferredItem<BlockItem>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH.get(c)));
            SAFETY_MESH_FLAMMABLE.put(c, ITEMS.registerSimpleBlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c)));
        }
    }

    private ModItems() {}
}
