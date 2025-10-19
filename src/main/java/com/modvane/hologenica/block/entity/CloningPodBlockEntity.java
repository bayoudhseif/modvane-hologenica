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

// Block entity for Cloning Pod - displays a static ragdoll model when complete
public class CloningPodBlockEntity extends BlockEntity {
    
    private String entityType = "";
    private int cloningTime = 0;
    private boolean hasRagdoll = false; // Whether a ragdoll is currently displayed
    private static final int CLONING_DURATION = 300; // 15 seconds (20 ticks per second)

    public CloningPodBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.CLONING_POD.get(), pos, state);
    }

    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;

        // If we have DNA to clone, start the cloning process
        if (!entityType.isEmpty() && cloningTime < CLONING_DURATION && !hasRagdoll) {
            cloningTime++;
            setChanged();
            
            // Debug: Log first tick of cloning
            if (cloningTime == 1) {
                com.modvane.hologenica.HologenicaMod.LOGGER.info("Starting cloning process for: {}", entityType);
            }
            
            // Sync to client every 5 ticks for smooth visual updates
            if (cloningTime % 5 == 0 && level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }

            // When cloning is complete, display the ragdoll
            if (cloningTime >= CLONING_DURATION) {
                com.modvane.hologenica.HologenicaMod.LOGGER.info("Cloning complete! Displaying ragdoll for: {}", entityType);
                displayRagdoll();
            }
        }
    }

    // Receive DNA from centrifuge
    public void receiveDNA(String entityTypeString) {
        if (entityType.isEmpty() || hasRagdoll) {
            this.entityType = entityTypeString;
            this.cloningTime = 0;
            this.hasRagdoll = false;
            setChanged();
            
            // Debug logging
            if (level != null && !level.isClientSide) {
                com.modvane.hologenica.HologenicaMod.LOGGER.info("Cloning Pod received DNA: {}", entityTypeString);
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    // Display the ragdoll inside the chamber
    private void displayRagdoll() {
        this.hasRagdoll = true;
        setChanged();
        
        // Sync to client for rendering
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // Clear the ragdoll (for future use if needed)
    public void clearRagdoll() {
        this.entityType = "";
        this.cloningTime = 0;
        this.hasRagdoll = false;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("EntityType", entityType);
        tag.putInt("CloningTime", cloningTime);
        tag.putBoolean("HasRagdoll", hasRagdoll);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        cloningTime = tag.getInt("CloningTime");
        hasRagdoll = tag.getBoolean("HasRagdoll");
    }

    // Sync data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EntityType", entityType);
        tag.putInt("CloningTime", cloningTime);
        tag.putBoolean("HasRagdoll", hasRagdoll);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public String getEntityType() {
        return entityType;
    }

    public int getCloningTime() {
        return cloningTime;
    }

    public int getCloningDuration() {
        return CLONING_DURATION;
    }

    public boolean isCloning() {
        return !entityType.isEmpty() && cloningTime < CLONING_DURATION && !hasRagdoll;
    }

    public boolean hasRagdoll() {
        return hasRagdoll;
    }
    
    // Get progress as a percentage (0.0 to 1.0)
    public float getCloningProgress() {
        if (hasRagdoll || entityType.isEmpty()) return 1.0f;
        return (float) cloningTime / (float) CLONING_DURATION;
    }
}

