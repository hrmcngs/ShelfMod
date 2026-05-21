package io.github.hrmcngs.shelfmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.ScheduledTickAccess;
import org.jetbrains.annotations.Nullable;

public class PoleScaffoldBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty COUNT_X = IntegerProperty.create("count_x", 0, 4);
    public static final IntegerProperty COUNT_Y = IntegerProperty.create("count_y", 0, 4);
    public static final IntegerProperty COUNT_Z = IntegerProperty.create("count_z", 0, 4);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

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

    public PoleScaffoldBlock(double halfWidth, Properties props) {
        super(props);
        this.shapesY = buildShapes(halfWidth, CENTRES_Y, ShapeKind.Y);
        this.shapesX = buildShapes(halfWidth, CENTRES_X, ShapeKind.X);
        this.shapesZ = buildShapes(halfWidth, CENTRES_Z, ShapeKind.Z);
        registerDefaultState(stateDefinition.any()
                .setValue(COUNT_X, 0).setValue(COUNT_Y, 1).setValue(COUNT_Z, 0)
                .setValue(WATERLOGGED, false));
    }

    private enum ShapeKind { Y, X, Z }

    private static VoxelShape[] buildShapes(double hw, double[][] centres, ShapeKind kind) {
        VoxelShape[] out = new VoxelShape[5];
        out[0] = Shapes.empty();
        for (int n = 1; n <= 4; n++) {
            VoxelShape s = Shapes.empty();
            double[] cs = centres[n];
            for (int i = 0; i < cs.length; i += 2) {
                double a = cs[i], b = cs[i + 1];
                VoxelShape box = switch (kind) {
                    case Y -> box(a - hw, 0, b - hw, a + hw, 16, b + hw);
                    case X -> box(0, a - hw, b - hw, 16, a + hw, b + hw);
                    case Z -> box(a - hw, b - hw, 0, a + hw, b + hw, 16);
                };
                s = Shapes.joinUnoptimized(s, box, BooleanOp.OR);
            }
            out[n] = s.optimize();
        }
        return out;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(COUNT_X, COUNT_Y, COUNT_Z, WATERLOGGED);
    }

    public static IntegerProperty countProp(Direction.Axis axis) {
        return switch (axis) {
            case X -> COUNT_X;
            case Y -> COUNT_Y;
            case Z -> COUNT_Z;
        };
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        if (ctx.isSecondaryUseActive()) return false;
        if (!ctx.getItemInHand().is(this.asItem())) return false;
        return state.getValue(countProp(ctx.getClickedFace().getAxis())) < 4;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Direction.Axis axis = ctx.getClickedFace().getAxis();
        if (existing.is(this)) {
            IntegerProperty prop = countProp(axis);
            return existing.setValue(prop, Math.min(4, existing.getValue(prop) + 1));
        }
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return defaultBlockState()
                .setValue(COUNT_X, 0).setValue(COUNT_Y, 0).setValue(COUNT_Z, 0)
                .setValue(countProp(axis), 1)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        VoxelShape shape = Shapes.empty();
        int cx = s.getValue(COUNT_X), cy = s.getValue(COUNT_Y), cz = s.getValue(COUNT_Z);
        if (cy > 0) shape = Shapes.joinUnoptimized(shape, shapesY[cy], BooleanOp.OR);
        if (cx > 0) shape = Shapes.joinUnoptimized(shape, shapesX[cx], BooleanOp.OR);
        if (cz > 0) shape = Shapes.joinUnoptimized(shape, shapesZ[cz], BooleanOp.OR);
        return shape.optimize();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return getShape(s, l, p, c);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return true; }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean isMoving) {
        if (!level.isClientSide) level.scheduleTick(pos, this, STABILITY_DELAY);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction dir, BlockPos nPos,
                                     BlockState ns, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        ticks.scheduleTick(pos, this, NEIGHBOR_DELAY);
        return state;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isStable(state, level, pos)) FallingBlockEntity.fall(level, pos, state);
    }

    private boolean isStable(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(COUNT_Y) > 0 && chainEnd(level, pos, Direction.Axis.Y, Direction.DOWN)) return true;
        if (state.getValue(COUNT_X) > 0
                && chainEnd(level, pos, Direction.Axis.X, Direction.EAST)
                && chainEnd(level, pos, Direction.Axis.X, Direction.WEST)) return true;
        if (state.getValue(COUNT_Z) > 0
                && chainEnd(level, pos, Direction.Axis.Z, Direction.SOUTH)
                && chainEnd(level, pos, Direction.Axis.Z, Direction.NORTH)) return true;
        return false;
    }

    private boolean chainEnd(LevelReader level, BlockPos pos, Direction.Axis axis, Direction step) {
        BlockPos walker = pos;
        IntegerProperty prop = countProp(axis);
        for (int i = 0; i < CHAIN_MAX; i++) {
            BlockPos next = walker.relative(step);
            BlockState ns = level.getBlockState(next);
            if (ns.getBlock() instanceof PoleScaffoldBlock && ns.getValue(prop) > 0) {
                walker = next;
                continue;
            }
            return ns.isFaceSturdy(level, next, step.getOpposite());
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> state
                    .setValue(COUNT_X, state.getValue(COUNT_Z))
                    .setValue(COUNT_Z, state.getValue(COUNT_X));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) { return state; }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
