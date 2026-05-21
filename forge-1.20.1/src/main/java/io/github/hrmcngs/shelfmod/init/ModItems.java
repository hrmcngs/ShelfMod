package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ScaffoldingBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ShelfMod.MODID);

    public static final RegistryObject<Item> BAMBOO_SCAFFOLD = ITEMS.register("bamboo_scaffold",
            () -> new ScaffoldingBlockItem(ModBlocks.BAMBOO_SCAFFOLD.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_SCAFFOLD = ITEMS.register("iron_scaffold",
            () -> new ScaffoldingBlockItem(ModBlocks.IRON_SCAFFOLD.get(), new Item.Properties()));

    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, ITEMS.register(c.id() + "_safety_mesh",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH.get(c).get(), new Item.Properties())));
            SAFETY_MESH_FLAMMABLE.put(c, ITEMS.register(c.id() + "_safety_mesh_flammable",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c).get(), new Item.Properties())));
        }
    }

    private ModItems() {}
}
