package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Stores the map region and cached terrain scan for holographic display
public class HolographicMapBlockEntity extends BlockEntity {

    private static final int SCAN_SIZE = 32;
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;

    // Cached terrain data to avoid rescanning every frame
    private BlockState[][][] cachedTerrain = null;
    private boolean needsRescan = true;

    // Toggle between transparent and solid rendering mode
    private boolean transparentMode = true;

    public HolographicMapBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.HOLOGRAPHIC_MAP.get(), pos, state);
    }

    // Setup region automatically based on scan size
    public void setupAutoRegion() {
        int halfSize = SCAN_SIZE / 2;
        BlockPos center = getBlockPos();
        pos1 = center.offset(-halfSize, 0, -halfSize);
        pos2 = center.offset(halfSize, 0, halfSize);
        needsRescan = true;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // Get cached terrain data (null if needs rescan)
    public BlockState[][][] getCachedTerrain() {
        return cachedTerrain;
    }

    // Update the cached terrain data
    public void setCachedTerrain(BlockState[][][] terrain) {
        this.cachedTerrain = terrain;
        this.needsRescan = false;
    }

    // Check if terrain needs to be rescanned
    public boolean needsRescan() {
        return needsRescan;
    }

    // Force a rescan on next render
    public void markForRescan() {
        this.needsRescan = true;
    }

    // Check if hologram is in transparent mode
    public boolean isTransparentMode() {
        return transparentMode;
    }

    // Toggle between transparent and solid rendering
    public void toggleTransparency() {
        this.transparentMode = !this.transparentMode;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // Get the first corner of the map region
    public BlockPos getPos1() {
        return pos1;
    }

    // Get the second corner of the map region
    public BlockPos getPos2() {
        return pos2;
    }

    // Check if a valid region is set
    public boolean hasRegion() {
        return pos1 != null && pos2 != null;
    }

    // Save the region to disk
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pos1 != null) {
            tag.putLong("Pos1", pos1.asLong());
        }
        if (pos2 != null) {
            tag.putLong("Pos2", pos2.asLong());
        }
        tag.putBoolean("TransparentMode", transparentMode);
    }

    // Load the region from disk
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Pos1")) {
            pos1 = BlockPos.of(tag.getLong("Pos1"));
        }
        if (tag.contains("Pos2")) {
            pos2 = BlockPos.of(tag.getLong("Pos2"));
        }
        if (tag.contains("TransparentMode")) {
            transparentMode = tag.getBoolean("TransparentMode");
        }
    }

    // Send data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (pos1 != null) {
            tag.putLong("Pos1", pos1.asLong());
        }
        if (pos2 != null) {
            tag.putLong("Pos2", pos2.asLong());
        }
        tag.putBoolean("TransparentMode", transparentMode);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
