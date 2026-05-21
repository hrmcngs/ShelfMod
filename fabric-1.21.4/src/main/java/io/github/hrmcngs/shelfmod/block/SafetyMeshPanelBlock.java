package io.github.hrmcngs.shelfmod.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class SafetyMeshPanelBlock extends Block implements Waterloggable {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final VoxelShape SHAPE_NORTH = createCuboidShape(0, 0, 14.5, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = createCuboidShape(0, 0, 0, 16, 16, 1.5);
    private static final VoxelShape SHAPE_EAST  = createCuboidShape(0, 0, 0, 1.5, 16, 16);
    private static final VoxelShape SHAPE_WEST  = createCuboidShape(14.5, 0, 0, 16, 16, 16);

    private final boolean flammable;
    private final boolean heatproof;

    public SafetyMeshPanelBlock(boolean flammable, boolean heatproof, AbstractBlock.Settings settings) {
        super(settings);
        this.flammable = flammable;
        this.heatproof = heatproof;
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> b) {
        b.add(FACING, WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState s, BlockView w, BlockPos p, ShapeContext c) {
        return switch (s.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_NORTH;
        };
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction clicked = ctx.getSide();
        if (clicked.getAxis().isVertical()) return null;
        FluidState fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockState state = getDefaultState()
                .with(FACING, clicked)
                .with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
        return canPlaceAt(state, ctx.getWorld(), ctx.getBlockPos()) ? state : null;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        BlockState supportState = world.getBlockState(supportPos);
        return isValidSupport(supportState, world, supportPos, facing);
    }

    private static boolean isValidSupport(BlockState s, WorldView world, BlockPos pos, Direction facing) {
        if (s.isIn(BlockTags.FENCES)) return false;
        if (s.isIn(BlockTags.WALLS))  return false;
        if (s.getBlock() instanceof PaneBlock) return false;
        if (s.getBlock() instanceof PoleScaffoldBlock) return true;
        if (s.getBlock() instanceof BraceScaffoldBlock) return true;
        return s.isSideSolidFullSquare(world, pos, facing);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView ticks,
                                                   BlockPos pos, Direction dir, BlockPos neighborPos,
                                                   BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            ticks.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return canPlaceAt(state, world, pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public boolean isFlammableMesh() { return flammable; }
    public boolean isHeatproof()     { return heatproof; }
    public int burnChance()          { return !flammable ? 0 : (heatproof ? 30 : 60); }
    public int spreadChance()        { return !flammable ? 0 : (heatproof ? 15 : 30); }
}
