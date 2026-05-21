package io.github.hrmcngs.shelfmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Diagonal Kao Jue brace (斜撑) — a thin bamboo strut placed at 45° in the X-Z plane.
 * Two orientations: NE-SW and NW-SE. The strut runs corner-to-corner at mid block height.
 *
 * Stability: brace survives if any neighbour is a scaffold pole, another brace, or any
 * sturdy face. Otherwise it falls. (Real braces are lashed to nearby poles — being lax
 * on the placement check keeps building HK-style truss patterns playable.)
 */
public class BraceScaffoldBlock extends Block implements SimpleWaterloggedBlock {
    public enum DiagAxis implements StringRepresentable {
        NE_SW, NW_SE;
        @Override public String getSerializedName() { return name().toLowerCase(); }
    }

    public static final EnumProperty<DiagAxis> DIAG_AXIS = EnumProperty.create("axis", DiagAxis.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Loose selection box around where the rotated pole lives. Collision is empty so the
    // brace doesn't trap the player in a half-built scaffold.
    private static final VoxelShape OUTLINE = box(2, 7, 2, 14, 9, 14);
    private static final int STABILITY_DELAY = 40;
    private static final int NEIGHBOR_DELAY = 4;

    public BraceScaffoldBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(DIAG_AXIS, DiagAxis.NE_SW)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(DIAG_AXIS, WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Pick the diagonal whose run direction is closer to the player's horizontal facing.
        Direction facing = ctx.getHorizontalDirection();
        DiagAxis ax = (facing.getAxis() == Direction.Axis.Z) ? DiagAxis.NE_SW : DiagAxis.NW_SE;
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return defaultBlockState()
                .setValue(DIAG_AXIS, ax)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

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
        if (!hasAnySupport(level, pos)) {
            FallingBlockEntity.fall(level, pos, state);
        }
    }

    private boolean hasAnySupport(LevelReader level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            BlockState ns = level.getBlockState(n);
            if (ns.getBlock() instanceof PoleScaffoldBlock) return true;
            if (ns.getBlock() instanceof BraceScaffoldBlock) return true;
            if (ns.isFaceSturdy(level, n, d.getOpposite())) return true;
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 ->
                    state.setValue(DIAG_AXIS, state.getValue(DIAG_AXIS) == DiagAxis.NE_SW
                            ? DiagAxis.NW_SE : DiagAxis.NE_SW);
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : rotate(state, Rotation.CLOCKWISE_180);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
