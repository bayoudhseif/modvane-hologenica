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

// Stores the map region to display as hologram
public class HolographicMapBlockEntity extends BlockEntity {

    private static final int SCAN_SIZE = 32;
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;

    public HolographicMapBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.HOLOGRAPHIC_MAP.get(), pos, state);
    }

    // Setup region automatically based on scan size
    public void setupAutoRegion() {
        int halfSize = SCAN_SIZE / 2;
        BlockPos center = getBlockPos();
        pos1 = center.offset(-halfSize, 0, -halfSize);
        pos2 = center.offset(halfSize, 0, halfSize);
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
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
