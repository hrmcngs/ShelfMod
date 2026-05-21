package io.github.hrmcngs.shelfmod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class PoleScaffoldBlock extends Block implements Waterloggable {
    public static final IntProperty COUNT_X = IntProperty.of("count_x", 0, 4);
    public static final IntProperty COUNT_Y = IntProperty.of("count_y", 0, 4);
    public static final IntProperty COUNT_Z = IntProperty.of("count_z", 0, 4);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final int STABILITY_DELAY = 40;
    private static final int NEIGHBOR_DELAY = 4;
    private static final int CHAIN_MAX = 64;

    private static final double[][] CENTRES_Y = {
            {}, {8, 8}, {4, 8, 12, 8}, {3, 8, 8, 8, 13, 8}, {4, 4, 12, 4, 4, 12, 12, 12},
    };
    private static final double[][] CENTRES_X = {
            {}, {8, 8}, {13, 4, 13, 12}, {13, 3, 13, 8, 13, 13}, {14, 2, 14, 6, 14, 10, 14, 14},
    };
    private static final double[][] CENTRES_Z = {
            {}, {8, 8}, {4, 13, 12, 13}, {3, 13, 8, 13, 13, 13}, {2, 14, 6, 14, 10, 14, 14, 14},
    };

    private final VoxelShape[] shapesY;
    private final VoxelShape[] shapesX;
    private final VoxelShape[] shapesZ;

    public PoleScaffoldBlock(double halfWidth, AbstractBlock.Settings props) {
        super(props);
        this.shapesY = buildShapes(halfWidth, CENTRES_Y, Axis.Y);
        this.shapesX = buildShapes(halfWidth, CENTRES_X, Axis.X);
        this.shapesZ = buildShapes(halfWidth, CENTRES_Z, Axis.Z);
        setDefaultState(getDefaultState()
                .with(COUNT_X, 0).with(COUNT_Y, 1).with(COUNT_Z, 0)
                .with(WATERLOGGED, false));
    }

    private enum Axis { Y, X, Z }

    private static VoxelShape[] buildShapes(double hw, double[][] centres, Axis kind) {
        VoxelShape[] out = new VoxelShape[5];
        out[0] = VoxelShapes.empty();
        for (int n = 1; n <= 4; n++) {
            VoxelShape s = VoxelShapes.empty();
            double[] cs = centres[n];
            for (int i = 0; i < cs.length; i += 2) {
                double a = cs[i], b = cs[i + 1];
                VoxelShape box = switch (kind) {
                    case Y -> createCuboidShape(a - hw, 0, b - hw, a + hw, 16, b + hw);
                    case X -> createCuboidShape(0, a - hw, b - hw, 16, a + hw, b + hw);
                    case Z -> createCuboidShape(a - hw, b - hw, 0, a + hw, b + hw, 16);
                };
                s = VoxelShapes.combine(s, box, BooleanBiFunction.OR);
            }
            out[n] = s.simplify();
        }
        return out;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> b) {
        b.add(COUNT_X, COUNT_Y, COUNT_Z, WATERLOGGED);
    }

    public static IntProperty countProp(Direction.Axis axis) {
        return switch (axis) {
            case X -> COUNT_X;
            case Y -> COUNT_Y;
            case Z -> COUNT_Z;
        };
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
        if (ctx.shouldCancelInteraction()) return false;
        if (!ctx.getStack().isOf(this.asItem())) return false;
        return state.get(countProp(ctx.getSide().getAxis())) < 4;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState existing = ctx.getWorld().getBlockState(ctx.getBlockPos());
        Direction.Axis axis = ctx.getSide().getAxis();
        if (existing.isOf(this)) {
            IntProperty prop = countProp(axis);
            return existing.with(prop, Math.min(4, existing.get(prop) + 1));
        }
        FluidState fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return getDefaultState()
                .with(COUNT_X, 0).with(COUNT_Y, 0).with(COUNT_Z, 0)
                .with(countProp(axis), 1)
                .with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState s, BlockView w, BlockPos p, net.minecraft.block.ShapeContext c) {
        VoxelShape shape = VoxelShapes.empty();
        int cx = s.get(COUNT_X), cy = s.get(COUNT_Y), cz = s.get(COUNT_Z);
        if (cy > 0) shape = VoxelShapes.combine(shape, shapesY[cy], BooleanBiFunction.OR);
        if (cx > 0) shape = VoxelShapes.combine(shape, shapesX[cx], BooleanBiFunction.OR);
        if (cz > 0) shape = VoxelShapes.combine(shape, shapesZ[cz], BooleanBiFunction.OR);
        return shape.simplify();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockView w, BlockPos p, net.minecraft.block.ShapeContext c) {
        return getOutlineShape(s, w, p, c);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) { return true; }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState old, boolean isMoving) {
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
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!isStable(state, world, pos)) FallingBlockEntity.spawnFromBlock(world, pos, state);
    }

    private boolean isStable(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(COUNT_Y) > 0 && chainEnd(world, pos, Direction.Axis.Y, Direction.DOWN)) return true;
        if (state.get(COUNT_X) > 0
                && chainEnd(world, pos, Direction.Axis.X, Direction.EAST)
                && chainEnd(world, pos, Direction.Axis.X, Direction.WEST)) return true;
        if (state.get(COUNT_Z) > 0
                && chainEnd(world, pos, Direction.Axis.Z, Direction.SOUTH)
                && chainEnd(world, pos, Direction.Axis.Z, Direction.NORTH)) return true;
        return false;
    }

    private boolean chainEnd(WorldView world, BlockPos pos, Direction.Axis axis, Direction step) {
        BlockPos walker = pos;
        IntProperty prop = countProp(axis);
        for (int i = 0; i < CHAIN_MAX; i++) {
            BlockPos next = walker.offset(step);
            BlockState ns = world.getBlockState(next);
            if (ns.getBlock() instanceof PoleScaffoldBlock && ns.get(prop) > 0) {
                walker = next;
                continue;
            }
            return ns.isSideSolidFullSquare(world, next, step.getOpposite());
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return switch (rot) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 ->
                    state.with(COUNT_X, state.get(COUNT_Z)).with(COUNT_Z, state.get(COUNT_X));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) { return state; }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
}
