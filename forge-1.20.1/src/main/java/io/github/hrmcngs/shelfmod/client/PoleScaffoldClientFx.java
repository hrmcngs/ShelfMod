package io.github.hrmcngs.shelfmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public final class PoleScaffoldClientFx implements IClientBlockExtensions {
    public static final PoleScaffoldClientFx INSTANCE = new PoleScaffoldClientFx();

    // Vanilla destroy spawns ~16 particles per box × N boxes in the VoxelShape; with
    // count=4 on multiple axes this balloons to hundreds. Cap at a single block budget.
    private static final int PARTICLE_BUDGET = 24;

    private PoleScaffoldClientFx() {}

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (!(level instanceof ClientLevel cl)) return false;
        RandomSource r = level.random;
        for (int i = 0; i < PARTICLE_BUDGET; i++) {
            double dx = r.nextDouble();
            double dy = r.nextDouble();
            double dz = r.nextDouble();
            manager.add(new TerrainParticle(cl,
                    pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz,
                    dx - 0.5, dy - 0.5, dz - 0.5,
                    state));
        }
        return true;
    }
}
