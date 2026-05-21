package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {

    public static final RegistryKey<ItemGroup> SHELF_TAB = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(ShelfMod.MOD_ID, "shelf_tab"));

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, SHELF_TAB,
                ItemGroup.create(ItemGroup.Row.TOP, 0)
                        .displayName(Text.translatable("itemGroup.shelfmod.shelf_tab"))
                        .icon(() -> new ItemStack(ModItems.BAMBOO_SCAFFOLD_ITEM))
                        .entries((displayContext, entries) -> {
                            entries.add(ModItems.BAMBOO_SCAFFOLD_ITEM);
                            entries.add(ModItems.KAO_BAMBOO_SCAFFOLD_ITEM);
                            entries.add(ModItems.BAMBOO_BRACE_ITEM);
                            entries.add(ModItems.IRON_SCAFFOLD_ITEM);
                            for (MeshColor c : MeshColor.values()) {
                                entries.add(ModItems.SAFETY_MESH_ITEM.get(c));
                                entries.add(ModItems.SAFETY_MESH_FLAMMABLE_ITEM.get(c));
                                entries.add(ModItems.SAFETY_MESH_HEATPROOF_ITEM.get(c));
                                entries.add(ModItems.SAFETY_MESH_FLAMMABLE_HEATPROOF_ITEM.get(c));
                            }
                        })
                        .build());
    }

    private ModItemGroups() {}
}
