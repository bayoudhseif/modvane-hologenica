package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Reconstruction Pod - grows clones from 1% to 100% then spawns them as real entities
public class ReconstructionPodBlockEntity extends BlockEntity {
    
    private String entityType = "";
    private int reconstructionProgress = 0;
    private int reconstructionDuration = 0; // Dynamic duration based on entity health
    private boolean isReconstructing = false;

    public ReconstructionPodBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.RECONSTRUCTION_POD.get(), pos, state);
    }

    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;

        // If not reconstructing, check for adjacent cloning chamber with ragdoll
        if (!isReconstructing) {
            checkForAdjacentCloningChamber();
        }

        // If reconstructing, increase progress
        if (isReconstructing && !entityType.isEmpty() && reconstructionDuration > 0) {
            reconstructionProgress++;
            setChanged();

            // Sync to client every 5 ticks for smooth visual updates
            if (reconstructionProgress % 5 == 0) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }

            // When complete, spawn the entity
            if (reconstructionProgress >= reconstructionDuration) {
                spawnClonedEntity();
            }
        }
    }

    // Check all adjacent positions for a cloning chamber with a ragdoll
    private void checkForAdjacentCloningChamber() {
        if (level == null) return;

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            if (level.getBlockEntity(adjacentPos) instanceof CloningPodBlockEntity pod) {
                // If pod has a ragdoll, start reconstruction
                // Don't remove the ragdoll - it stays in the pod permanently
                if (pod.hasRagdoll() && !pod.getEntityType().isEmpty()) {
                    startReconstruction(pod.getEntityType());
                    return;
                }
            }
        }
    }

    // Start the reconstruction process
    private void startReconstruction(String entityTypeString) {
        this.entityType = entityTypeString;
        this.reconstructionProgress = 0;
        
        // Calculate duration based on entity's max health (health × 6 seconds × 20 ticks)
        this.reconstructionDuration = calculateReconstructionDuration(entityTypeString);
        
        this.isReconstructing = true;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Calculate reconstruction duration based on entity's max health
    // Formula: health × 6 seconds × 20 ticks per second
    private int calculateReconstructionDuration(String entityTypeString) {
        if (level == null) return 1200; // Default 60 seconds if we can't calculate
        
        try {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeString);
            EntityType<?> type = EntityType.byString(entityId.toString()).orElse(null);
            
            if (type != null) {
                // Create temporary entity to get its max health
                Entity entity = type.create(level);
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                    float maxHealth = livingEntity.getMaxHealth();
                    // health × 6 seconds × 20 ticks per second
                    return (int) (maxHealth * 6 * 20);
                }
            }
        } catch (Exception e) {
            // Failed to calculate, use default
        }
        
        // Default: 60 seconds (for 10 health entities)
        return 1200;
    }

    // Spawn the fully reconstructed entity
    private void spawnClonedEntity() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;
        if (entityType.isEmpty()) return;

        try {
            // Parse the entity type
            ResourceLocation entityId = ResourceLocation.parse(entityType);
            EntityType<?> type = EntityType.byString(entityId.toString()).orElse(null);
            
            if (type != null) {
                // Spawn position above the pod
                BlockPos spawnPos = worldPosition.above();
                
                // Make sure the spawn position is safe
                if (!serverLevel.getBlockState(spawnPos).isAir()) {
                    spawnPos = worldPosition.above(2);
                }
                
                // Spawn the entity
                Entity entity = type.create(serverLevel, null, spawnPos, MobSpawnType.SPAWNER, false, false);
                
                if (entity != null) {
                    // Position it in the center of the block
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    serverLevel.addFreshEntity(entity);
                }
            }
        } catch (Exception e) {
            // Failed to spawn entity - silently fail
        }

        // Reset the pod
        entityType = "";
        reconstructionProgress = 0;
        reconstructionDuration = 0;
        isReconstructing = false;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("EntityType", entityType);
        tag.putInt("ReconstructionProgress", reconstructionProgress);
        tag.putInt("ReconstructionDuration", reconstructionDuration);
        tag.putBoolean("IsReconstructing", isReconstructing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        reconstructionProgress = tag.getInt("ReconstructionProgress");
        reconstructionDuration = tag.getInt("ReconstructionDuration");
        isReconstructing = tag.getBoolean("IsReconstructing");
    }

    // Sync data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EntityType", entityType);
        tag.putInt("ReconstructionProgress", reconstructionProgress);
        tag.putInt("ReconstructionDuration", reconstructionDuration);
        tag.putBoolean("IsReconstructing", isReconstructing);
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

    public int getReconstructionProgress() {
        return reconstructionProgress;
    }

    public boolean isReconstructing() {
        return isReconstructing;
    }

    // Get progress as a percentage (0.0 to 1.0)
    public float getProgressPercentage() {
        if (reconstructionDuration <= 0) return 0.0f;
        return (float) reconstructionProgress / (float) reconstructionDuration;
    }
    
    public int getReconstructionDuration() {
        return reconstructionDuration;
    }
}

