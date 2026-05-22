package io.github.hrmcngs.shelfmod.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class PoleScaffoldClientFx {
    // Vanilla destroy spawns ~16 particles per box × N boxes in the VoxelShape; with
    // count=4 on multiple axes this balloons to hundreds. Cap at a single block budget.
    private static final int PARTICLE_BUDGET = 24;

    private PoleScaffoldClientFx() {}

    public static void spawnLimitedBreakParticles(BlockPos pos, BlockState state) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;
        Random r = world.random;
        BlockStateParticleEffect effect = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        for (int i = 0; i < PARTICLE_BUDGET; i++) {
            double dx = r.nextDouble();
            double dy = r.nextDouble();
            double dz = r.nextDouble();
            world.addParticle(effect,
                    pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz,
                    dx - 0.5, dy - 0.5, dz - 0.5);
        }
    }
}
