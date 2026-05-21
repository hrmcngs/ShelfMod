package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class ModItems {

    public static final Item BAMBOO_SCAFFOLD_ITEM = register(
            "bamboo_scaffold",
            settings -> new BlockItem(ModBlocks.BAMBOO_SCAFFOLD, settings),
            new Item.Settings());

    public static final Item KAO_BAMBOO_SCAFFOLD_ITEM = register(
            "kao_bamboo_scaffold",
            settings -> new BlockItem(ModBlocks.KAO_BAMBOO_SCAFFOLD, settings),
            new Item.Settings());

    public static final Item BAMBOO_BRACE_ITEM = register(
            "bamboo_brace",
            settings -> new BlockItem(ModBlocks.BAMBOO_BRACE, settings),
            new Item.Settings());

    public static final Item IRON_SCAFFOLD_ITEM = register(
            "iron_scaffold",
            settings -> new BlockItem(ModBlocks.IRON_SCAFFOLD, settings),
            new Item.Settings());

    public static final Map<MeshColor, Item> SAFETY_MESH_ITEM = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Item> SAFETY_MESH_FLAMMABLE_ITEM = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Item> SAFETY_MESH_HEATPROOF_ITEM = new EnumMap<>(MeshColor.class);
    public static final Map<MeshColor, Item> SAFETY_MESH_FLAMMABLE_HEATPROOF_ITEM = new EnumMap<>(MeshColor.class);

    static {
        for (MeshColor c : MeshColor.values()) {
            SAFETY_MESH_ITEM.put(c, register(
                    c.id() + "_safety_mesh",
                    s -> new BlockItem(ModBlocks.SAFETY_MESH.get(c), s),
                    new Item.Settings()));
            SAFETY_MESH_FLAMMABLE_ITEM.put(c, register(
                    c.id() + "_safety_mesh_flammable",
                    s -> new BlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE.get(c), s),
                    new Item.Settings()));
            SAFETY_MESH_HEATPROOF_ITEM.put(c, register(
                    c.id() + "_safety_mesh_heatproof",
                    s -> new BlockItem(ModBlocks.SAFETY_MESH_HEATPROOF.get(c), s),
                    new Item.Settings()));
            SAFETY_MESH_FLAMMABLE_HEATPROOF_ITEM.put(c, register(
                    c.id() + "_safety_mesh_flammable_heatproof",
                    s -> new BlockItem(ModBlocks.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c), s),
                    new Item.Settings()));
        }
    }

    private static <T extends Item> T register(String name,
                                               Function<Item.Settings, T> factory,
                                               Item.Settings settings) {
        Identifier id = Identifier.of(ShelfMod.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        T item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void init() {}

    private ModItems() {}
}
