package io.github.hrmcngs.shelfmod.init;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;

public enum MeshColor {
    GREEN ("green",  MapColor.COLOR_GREEN,      DyeColor.GREEN),
    BLACK ("black",  MapColor.COLOR_BLACK,      DyeColor.BLACK),
    WHITE ("white",  MapColor.SNOW,             DyeColor.WHITE),
    BLUE  ("blue",   MapColor.COLOR_BLUE,       DyeColor.BLUE),
    SILVER("silver", MapColor.COLOR_LIGHT_GRAY, DyeColor.LIGHT_GRAY),
    GRAY  ("gray",   MapColor.COLOR_GRAY,       DyeColor.GRAY);

    private final String id;
    private final MapColor mapColor;
    private final DyeColor dye;

    MeshColor(String id, MapColor mapColor, DyeColor dye) {
        this.id = id;
        this.mapColor = mapColor;
        this.dye = dye;
    }

    public String id() { return id; }
    public MapColor mapColor() { return mapColor; }
    public DyeColor dye() { return dye; }
}
