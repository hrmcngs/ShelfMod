package io.github.hrmcngs.shelfmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Painting-style safety mesh: a thin panel pasted onto a single horizontal face of a
 * supporting block. Two orthogonal grade axes:
 *
 *   - {@code flammable}: catches fire from adjacent flames (mirrors HK 防燃 / 可燃 categories)
 *   - {@code heatproof}: heat-resistant fibre. Halves flammability spread when flammable,
 *                       is otherwise a lore/spec marker for the highest grade mesh.
 *
 * Supported on solid block faces and on {@link PoleScaffoldBlock} / {@link BraceScaffoldBlock};
 * explicitly rejected on vanilla fences, walls and iron-bars-family panes.
 */
public class SafetyMeshPanelBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_NORTH = box(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = box(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_EAST  = box(0, 0, 0, 1.5, 16, 16);
    private static final VoxelShape SHAPE_WEST  = box(14.5, 0, 0, 16, 16, 16);

    private final boolean flammable;
    private final boolean heatproof;

    public SafetyMeshPanelBlock(boolean flammable, boolean heatproof, Properties props) {
        super(props);
        this.flammable = flammable;
        this.heatproof = heatproof;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return switch (s.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clicked = ctx.getClickedFace();
        if (clicked.getAxis().isVertical()) return null;

        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        BlockState state = defaultBlockState()
                .setValue(FACING, clicked)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);

        return canSurvive(state, ctx.getLevel(), ctx.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return isValidSupport(supportState, level, supportPos, facing);
    }

    private static boolean isValidSupport(BlockState s, LevelReader level, BlockPos pos, Direction facing) {
        if (s.is(BlockTags.FENCES)) return false;
        if (s.is(BlockTags.WALLS))  return false;
        if (s.getBlock() instanceof IronBarsBlock) return false;
        if (s.getBlock() instanceof PoleScaffoldBlock) return true;
        if (s.getBlock() instanceof BraceScaffoldBlock) return true;
        return s.isFaceSturdy(level, pos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ---- Flammability (Forge IForgeBlock overrides). ----

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        return flammable;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!flammable) return 0;
        return heatproof ? 30 : 60;        // heat-proof halves burn rate
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
        if (!flammable) return 0;
        return heatproof ? 15 : 30;
    }

    public boolean isHeatproof() { return heatproof; }
    public boolean isMeshFlammable() { return flammable; }
}
