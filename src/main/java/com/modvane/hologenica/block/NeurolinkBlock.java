package com.modvane.hologenica.block;

import com.mojang.serialization.MapCodec;
import com.modvane.hologenica.block.entity.NeurocellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Neurolink block - connects like redstone, only for this mod's blocks
public class NeurolinkBlock extends Block {

    public static final MapCodec<NeurolinkBlock> CODEC = simpleCodec(NeurolinkBlock::new);

    // Connection properties for all 6 directions
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    // Center dot shape (6,1,6 to 10,5,10)
    private static final VoxelShape CENTER_SHAPE = Block.box(6.0, 1.0, 6.0, 10.0, 5.0, 10.0);
    
    // Connection arm shapes for each direction
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0, 1.0, 0.0, 10.0, 5.0, 6.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0, 1.0, 10.0, 10.0, 5.0, 16.0);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0, 1.0, 6.0, 16.0, 5.0, 10.0);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0, 1.0, 6.0, 6.0, 5.0, 10.0);
    private static final VoxelShape UP_SHAPE = Block.box(6.0, 5.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 11.0, 10.0);

    public NeurolinkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(EAST, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return getConnectionState(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.setBlock(pos, getConnectionState(state, level, pos), 3);
        }
    }

    // Calculate which sides should connect
    private BlockState getConnectionState(BlockState state, LevelAccessor level, BlockPos pos) {
        boolean north = canConnectTo(level, pos, Direction.NORTH);
        boolean south = canConnectTo(level, pos, Direction.SOUTH);
        boolean east = canConnectTo(level, pos, Direction.EAST);
        boolean west = canConnectTo(level, pos, Direction.WEST);
        boolean up = canConnectTo(level, pos, Direction.UP);
        boolean down = canConnectTo(level, pos, Direction.DOWN);

        return state
            .setValue(NORTH, north)
            .setValue(SOUTH, south)
            .setValue(EAST, east)
            .setValue(WEST, west)
            .setValue(UP, up)
            .setValue(DOWN, down);
    }

    // Check if this neurolink can connect in a direction
    private boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        Block neighborBlock = neighborState.getBlock();

        // Connect to other neurolinks
        if (neighborBlock instanceof NeurolinkBlock) {
            return true;
        }

        // Connect to neurocells (any side except neurocell's front)
        if (neighborBlock instanceof NeurocellBlock) {
            var blockEntity = level.getBlockEntity(neighborPos);
            if (blockEntity instanceof NeurocellBlockEntity neurocell) {
                return neurocell.acceptsConnectionFrom(direction.getOpposite());
            }
        }

        // Connect to reformers and imprinters (any side)
        if (neighborBlock instanceof ReformerBlock || neighborBlock instanceof ImprinterBlock) {
            return true;
        }

        return false;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Start with the center dot
        VoxelShape shape = CENTER_SHAPE;
        
        // Add arm shapes for each connected direction
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_SHAPE);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_SHAPE);
        }
        
        return shape;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
}
