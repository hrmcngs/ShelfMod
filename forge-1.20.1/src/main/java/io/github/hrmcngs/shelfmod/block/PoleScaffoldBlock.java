package io.github.hrmcngs.shelfmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Multi-pole scaffolding block.
 *
 * Each block may contain up to 4 parallel poles per axis (independently for X / Y / Z).
 * The pole diameter is configurable per material via {@code halfWidth} so the same class
 * can back the thick HK {@code 毛竹} (Mao Jue, ~3px standards) and the thinner {@code 篙竹}
 * (Kao Jue, ~2px ledgers / braces).
 *
 * Clicking the face of an existing pole block with the matching item increments the count
 * on that axis — so the same block can grow into a 4-corner tower, a 4-beam horizontal
 * deck, or a cross of vertical + horizontal poles.
 */
public class PoleScaffoldBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty COUNT_X = IntegerProperty.create("count_x", 0, 4);
    public static final IntegerProperty COUNT_Y = IntegerProperty.create("count_y", 0, 4);
    public static final IntegerProperty COUNT_Z = IntegerProperty.create("count_z", 0, 4);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final int STABILITY_DELAY = 40;
    private static final int NEIGHBOR_DELAY = 4;

    // Slot centres in 0..16 voxel space. Each entry is [(cx0,cz0), (cx1,cz1), ...].
    // Y-axis slots: vary cx/cz in the X-Z plane.
    private static final double[][] CENTRES_Y = {
            {},
            {8, 8},
            {4, 8, 12, 8},
            {3, 8, 8, 8, 13, 8},
            {4, 4, 12, 4, 4, 12, 12, 12},
    };
    // X-axis slots: pole runs along X. Vary cy/cz; layouts climb toward top of block
    // so increasing count produces a progressively flatter bridge deck.
    private static final double[][] CENTRES_X = {
            {},
            {8, 8},
            {13, 4, 13, 12},
            {13, 3, 13, 8, 13, 13},
            {14, 2, 14, 6, 14, 10, 14, 14},
    };
    // Z-axis slots: pole runs along Z. Vary cx/cy.
    private static final double[][] CENTRES_Z = {
            {},
            {8, 8},
            {4, 13, 12, 13},
            {3, 13, 8, 13, 13, 13},
            {2, 14, 6, 14, 10, 14, 14, 14},
    };

    private final VoxelShape[] shapesY;
    private final VoxelShape[] shapesX;
    private final VoxelShape[] shapesZ;

    public PoleScaffoldBlock(double halfWidth, Properties props) {
        super(props);
        this.shapesY = buildShapesY(halfWidth);
        this.shapesX = buildShapesX(halfWidth);
        this.shapesZ = buildShapesZ(halfWidth);
        registerDefaultState(stateDefinition.any()
                .setValue(COUNT_X, 0)
                .setValue(COUNT_Y, 1)
                .setValue(COUNT_Z, 0)
                .setValue(WATERLOGGED, false));
    }

    private static VoxelShape[] buildShapesY(double hw) {
        VoxelShape[] out = new VoxelShape[5];
        out[0] = Shapes.empty();
        for (int n = 1; n <= 4; n++) {
            VoxelShape s = Shapes.empty();
            double[] cs = CENTRES_Y[n];
            for (int i = 0; i < cs.length; i += 2) {
                double cx = cs[i], cz = cs[i + 1];
                s = Shapes.joinUnoptimized(s,
                        box(cx - hw, 0, cz - hw, cx + hw, 16, cz + hw), BooleanOp.OR);
            }
            out[n] = s.optimize();
        }
        return out;
    }

    private static VoxelShape[] buildShapesX(double hw) {
        VoxelShape[] out = new VoxelShape[5];
        out[0] = Shapes.empty();
        for (int n = 1; n <= 4; n++) {
            VoxelShape s = Shapes.empty();
            double[] cs = CENTRES_X[n];
            for (int i = 0; i < cs.length; i += 2) {
                double cy = cs[i], cz = cs[i + 1];
                s = Shapes.joinUnoptimized(s,
                        box(0, cy - hw, cz - hw, 16, cy + hw, cz + hw), BooleanOp.OR);
            }
            out[n] = s.optimize();
        }
        return out;
    }

    private static VoxelShape[] buildShapesZ(double hw) {
        VoxelShape[] out = new VoxelShape[5];
        out[0] = Shapes.empty();
        for (int n = 1; n <= 4; n++) {
            VoxelShape s = Shapes.empty();
            double[] cs = CENTRES_Z[n];
            for (int i = 0; i < cs.length; i += 2) {
                double cx = cs[i], cy = cs[i + 1];
                s = Shapes.joinUnoptimized(s,
                        box(cx - hw, cy - hw, 0, cx + hw, cy + hw, 16), BooleanOp.OR);
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

    // ---- Placement: same-block clicks increment count along the clicked face's axis. ----

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        // Shift + right-click cycles count 1→2→3→4→1; plain right-click places adjacent.
        if (!ctx.isSecondaryUseActive()) return false;
        if (!ctx.getItemInHand().is(this.asItem())) return false;
        return true;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState existing = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Direction.Axis axis = ctx.getClickedFace().getAxis();
        if (existing.is(this)) {
            IntegerProperty prop = countProp(axis);
            int current = existing.getValue(prop);
            int next = current >= 4 ? 1 : current + 1;
            return existing.setValue(prop, next);
        }
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return defaultBlockState()
                .setValue(COUNT_X, 0)
                .setValue(COUNT_Y, 0)
                .setValue(COUNT_Z, 0)
                .setValue(countProp(axis), 1)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    // ---- Shape: union of per-axis pole shapes. ----

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

    // ---- Stability: per-axis chain walk, OR-combined. ----

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean isMoving) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, STABILITY_DELAY);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState ns,
                                  LevelAccessor level, BlockPos pos, BlockPos nPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (level instanceof ServerLevel sl) {
            sl.scheduleTick(pos, this, NEIGHBOR_DELAY);
        }
        return state;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isStable(state, level, pos)) {
            FallingBlockEntity.fall(level, pos, state);
        }
    }

    /**
     * Lenient stability: any of the 6 cardinal neighbours is a scaffold block or has a
     * non-empty collision shape → stable. Prevents the falling-block flicker that occurred
     * with the previous chain-walk rule (block re-falling on every landing) and matches
     * the user's request: "ブロックが下にあれば、または隣接していれば落ちない".
     */
    private boolean isStable(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            BlockState ns = level.getBlockState(n);
            if (ns.getBlock() instanceof PoleScaffoldBlock) return true;
            if (ns.getBlock() instanceof BraceScaffoldBlock) return true;
            if (!ns.getCollisionShape(level, n).isEmpty()) return true;
        }
        // Brace acts as a long-reach anchor: scan up to BRACE_RANGE cells along each
        // cardinal direction and treat a brace as support, so a brace placed in a gap
        // between two pole supports keeps both standing.
        return scanForBrace(level, pos);
    }

    private static final int BRACE_RANGE = 3;

    static boolean scanForBrace(LevelReader level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction d : Direction.values()) {
            for (int i = 1; i <= BRACE_RANGE; i++) {
                cursor.set(pos.getX() + d.getStepX() * i,
                        pos.getY() + d.getStepY() * i,
                        pos.getZ() + d.getStepZ() * i);
                if (level.getBlockState(cursor).getBlock() instanceof BraceScaffoldBlock) return true;
            }
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
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(io.github.hrmcngs.shelfmod.client.PoleScaffoldClientFx.INSTANCE);
    }
}
