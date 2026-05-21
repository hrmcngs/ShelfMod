package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ShelfMod.MODID);

    public static final RegistryObject<Item> BAMBOO_SCAFFOLD = ITEMS.register("bamboo_scaffold",
            () -> new BlockItem(ModBlocks.BAMBOO_SCAFFOLD.get(), new Item.Properties()));

    public static final RegistryObject<Item> KAO_BAMBOO_SCAFFOLD = ITEMS.register("kao_bamboo_scaffold",
            () -> new BlockItem(ModBlocks.KAO_BAMBOO_SCAFFOLD.get(), new Item.Properties()));

    public static final RegistryObject<Item> BAMBOO_BRACE = ITEMS.register("bamboo_brace",
            () -> new BlockItem(ModBlocks.BAMBOO_BRACE.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_SCAFFOLD = ITEMS.register("iron_scaffold",
            () -> new BlockItem(ModBlocks.IRON_SCAFFOLD.get(), new Item.Properties()));

    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH_FLAMMABLE =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH_HEATPROOF =
            new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, RegistryObject<Item>> SAFETY_MESH_FLAMMABLE_HEATPROOF =
            new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH.put(c, ITEMS.register(c.id() + "_safety_mesh",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH.get(c).get(), new Item.Properties())));
            SAFETY_MESH_FLAMMABLE.put(c, ITEMS.register(c.id() + "_safety_mesh_flammable",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c).get(), new Item.Properties())));
            SAFETY_MESH_HEATPROOF.put(c, ITEMS.register(c.id() + "_safety_mesh_heatproof",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH_HEATPROOF.get(c).get(), new Item.Properties())));
            SAFETY_MESH_FLAMMABLE_HEATPROOF.put(c, ITEMS.register(c.id() + "_safety_mesh_flammable_heatproof",
                    () -> new BlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c).get(), new Item.Properties())));
        }
    }

    private ModItems() {}
}
