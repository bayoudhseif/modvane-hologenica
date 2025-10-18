package com.modvane.hologenica.block;

import com.modvane.hologenica.block.entity.HolographicMapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Block that displays a 3D holographic projection of nearby terrain
// Automatically scans a 32x32 area when placed
public class HolographicMapBlock extends Block implements EntityBlock {

    public HolographicMapBlock(Properties properties) {
        super(properties);
    }

    // Creates the block entity that stores scan data
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HolographicMapBlockEntity(pos, state);
    }

    // Use the block's model for rendering (hologram renders separately via BlockEntityRenderer)
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Automatically setup the scan region when the block is placed
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HolographicMapBlockEntity map) {
            map.setupScanRegion();
        }
    }

    // Right-click to open the holographic map GUI
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HolographicMapBlockEntity map) {
            player.openMenu(map.getMenuProvider());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
