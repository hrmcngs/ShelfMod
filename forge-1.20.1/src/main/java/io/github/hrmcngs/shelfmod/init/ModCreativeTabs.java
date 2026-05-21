package io.github.hrmcngs.shelfmod.init;

import io.github.hrmcngs.shelfmod.ShelfMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShelfMod.MODID);

    public static final RegistryObject<CreativeModeTab> SHELF_TAB = TABS.register("shelf_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.shelfmod.shelf_tab"))
                    .icon(() -> new ItemStack(ModItems.BAMBOO_SCAFFOLD.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BAMBOO_SCAFFOLD.get());
                        output.accept(ModItems.KAO_BAMBOO_SCAFFOLD.get());
                        output.accept(ModItems.BAMBOO_BRACE.get());
                        output.accept(ModItems.IRON_SCAFFOLD.get());
                        for (MeshColor c : MeshColor.values()) {
                            output.accept(ModItems.SAFETY_MESH.get(c).get());
                            output.accept(ModItems.SAFETY_MESH_FLAMMABLE.get(c).get());
                            output.accept(ModItems.SAFETY_MESH_HEATPROOF.get(c).get());
                            output.accept(ModItems.SAFETY_MESH_FLAMMABLE_HEATPROOF.get(c).get());
                        }
                    })
                    .build());

    private ModCreativeTabs() {}
}
