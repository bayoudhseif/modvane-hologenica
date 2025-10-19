package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.block.BridgeBlock;
import com.modvane.hologenica.block.CloningPodBlock;
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// Reconstruction Pod - grows clones from 1% to 100% then spawns them as real entities
public class ReconstructionPodBlockEntity extends BlockEntity {
    
    private String entityType = "";
    private String entityName = ""; // Name of the entity being reconstructed
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

        // If reconstructing, validate connection and increase progress
        if (isReconstructing && !entityType.isEmpty() && reconstructionDuration > 0) {
            // Check if connection to cloning pod is still valid
            CloningPodBlockEntity connectedPod = findConnectedCloningPod();
            
            if (connectedPod == null || !connectedPod.hasRagdoll() || connectedPod.getEntityType().isEmpty()) {
                // Connection lost or cloning pod no longer has ragdoll - stop reconstruction
                resetReconstruction();
                return;
            }
            
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

    // Search for a cloning pod connected via bridge blocks
    private void checkForAdjacentCloningChamber() {
        if (level == null) return;

        // Use breadth-first search to find connected cloning pods through bridges
        CloningPodBlockEntity connectedPod = findConnectedCloningPod();
        
        if (connectedPod != null && connectedPod.hasRagdoll() && !connectedPod.getEntityType().isEmpty()) {
            startReconstruction(connectedPod.getEntityType(), connectedPod.getEntityName());
        }
    }
    
    // Find a cloning pod connected through bridge blocks (max 32 blocks away)
    private CloningPodBlockEntity findConnectedCloningPod() {
        if (level == null) return null;
        
        Queue<BlockPos> toVisit = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        
        // Start from reconstruction pod position
        toVisit.add(worldPosition);
        visited.add(worldPosition);
        
        int maxDistance = 32; // Maximum search distance
        int currentDistance = 0;
        
        while (!toVisit.isEmpty() && currentDistance < maxDistance) {
            int levelSize = toVisit.size();
            
            for (int i = 0; i < levelSize; i++) {
                BlockPos current = toVisit.poll();
                
                // Check all horizontal directions (bridges connect horizontally)
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = current.relative(direction);
                    
                    // Skip if already visited
                    if (visited.contains(neighbor)) continue;
                    visited.add(neighbor);
                    
                    BlockState neighborState = level.getBlockState(neighbor);
                    
                    // Check if this is a cloning pod with a ragdoll
                    if (neighborState.getBlock() instanceof CloningPodBlock) {
                        BlockEntity be = level.getBlockEntity(neighbor);
                        if (be instanceof CloningPodBlockEntity pod) {
                            // Found a cloning pod - check if it has a ragdoll
                            if (pod.hasRagdoll() && !pod.getEntityType().isEmpty()) {
                                return pod;
                            }
                        }
                    }
                    
                    // If this is a bridge block, continue searching through it
                    if (neighborState.getBlock() instanceof BridgeBlock) {
                        toVisit.add(neighbor);
                    }
                }
            }
            
            currentDistance++;
        }
        
        return null; // No connected cloning pod found
    }

    // Start the reconstruction process
    private void startReconstruction(String entityTypeString, String entityNameString) {
        this.entityType = entityTypeString;
        this.entityName = entityNameString;
        this.reconstructionProgress = 0;
        
        // Calculate duration based on entity's max health (health × 6 seconds × 20 ticks)
        this.reconstructionDuration = calculateReconstructionDuration(entityTypeString);
        
        this.isReconstructing = true;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Reset reconstruction when connection is lost
    private void resetReconstruction() {
        this.entityType = "";
        this.entityName = "";
        this.reconstructionProgress = 0;
        this.reconstructionDuration = 0;
        this.isReconstructing = false;
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
            EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(entityId);
            
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
            EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(entityId);
            
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
                    // If it's a Steve NPC, set the owner name
                    if (entity instanceof com.modvane.hologenica.entity.SteveNPCEntity steveNPC && !entityName.isEmpty()) {
                        steveNPC.setOwnerName(entityName);
                    }
                    
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
        entityName = "";
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
        tag.putString("EntityName", entityName);
        tag.putInt("ReconstructionProgress", reconstructionProgress);
        tag.putInt("ReconstructionDuration", reconstructionDuration);
        tag.putBoolean("IsReconstructing", isReconstructing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        entityName = tag.getString("EntityName");
        reconstructionProgress = tag.getInt("ReconstructionProgress");
        reconstructionDuration = tag.getInt("ReconstructionDuration");
        isReconstructing = tag.getBoolean("IsReconstructing");
    }

    // Sync data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EntityType", entityType);
        tag.putString("EntityName", entityName);
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

