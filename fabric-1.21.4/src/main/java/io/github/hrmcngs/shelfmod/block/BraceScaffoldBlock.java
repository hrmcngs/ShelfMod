package io.github.hrmcngs.shelfmod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class BraceScaffoldBlock extends Block implements Waterloggable {
    public enum DiagAxis implements StringIdentifiable {
        NE_SW, NW_SE;
        @Override public String asString() { return name().toLowerCase(); }
    }

    public static final EnumProperty<DiagAxis> DIAG_AXIS = EnumProperty.of("axis", DiagAxis.class);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape OUTLINE = createCuboidShape(2, 7, 2, 14, 9, 14);
    private static final int STABILITY_DELAY = 40;
    private static final int NEIGHBOR_DELAY = 4;

    public BraceScaffoldBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(DIAG_AXIS, DiagAxis.NE_SW).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> b) {
        b.add(DIAG_AXIS, WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getHorizontalPlayerFacing();
        DiagAxis ax = (facing.getAxis() == Direction.Axis.Z) ? DiagAxis.NE_SW : DiagAxis.NW_SE;
        FluidState fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return getDefaultState().with(DIAG_AXIS, ax)
                .with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState s, BlockView w, BlockPos p, ShapeContext c) { return OUTLINE; }

    @Override
    protected VoxelShape getCollisionShape(BlockState s, BlockView w, BlockPos p, ShapeContext c) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) { return true; }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState old, boolean isMoving) {
        if (!world.isClient) world.scheduleBlockTick(pos, this, STABILITY_DELAY);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView ticks,
                                                   BlockPos pos, Direction dir, BlockPos nPos,
                                                   BlockState ns, Random random) {
        if (state.get(WATERLOGGED)) {
            ticks.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        ticks.scheduleBlockTick(pos, this, NEIGHBOR_DELAY);
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!hasAnySupport(world, pos)) FallingBlockEntity.spawnFromBlock(world, pos, state);
    }

    private boolean hasAnySupport(WorldView world, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.offset(d);
            BlockState ns = world.getBlockState(n);
            if (ns.getBlock() instanceof PoleScaffoldBlock) return true;
            if (ns.getBlock() instanceof BraceScaffoldBlock) return true;
            if (ns.isSideSolidFullSquare(world, n, d.getOpposite())) return true;
        }
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rot) {
        return switch (rot) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 ->
                    state.with(DIAG_AXIS, state.get(DIAG_AXIS) == DiagAxis.NE_SW ? DiagAxis.NW_SE : DiagAxis.NE_SW);
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return mirror == BlockMirror.NONE ? state : rotate(state, BlockRotation.CLOCKWISE_180);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
}
