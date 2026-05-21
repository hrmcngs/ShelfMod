package io.github.hrmcngs.shelfmod.init;

import net.minecraft.block.MapColor;
import net.minecraft.util.DyeColor;

public enum MeshColor {
    GREEN ("green",  MapColor.GREEN,            DyeColor.GREEN),
    BLACK ("black",  MapColor.BLACK,            DyeColor.BLACK),
    WHITE ("white",  MapColor.WHITE,            DyeColor.WHITE),
    BLUE  ("blue",   MapColor.BLUE,             DyeColor.BLUE),
    SILVER("silver", MapColor.LIGHT_GRAY,       DyeColor.LIGHT_GRAY),
    GRAY  ("gray",   MapColor.GRAY,             DyeColor.GRAY);

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
