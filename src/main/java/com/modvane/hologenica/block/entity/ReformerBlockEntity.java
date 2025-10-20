package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.util.NeurocellConnector;
import net.minecraft.core.BlockPos;
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

// Reformer - grows clones from 1% to 100% then spawns them as real entities
public class ReformerBlockEntity extends BlockEntity {

    private String entityType = "";
    private String entityName = ""; // Name of the entity being reconstructed
    private java.util.UUID playerUUID = null; // UUID of the player (for skin rendering)
    private int reconstructionProgress = 0;
    private int reconstructionDuration = 0; // Dynamic duration based on entity health
    private boolean isReconstructing = false;

    public ReformerBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.REFORMER.get(), pos, state);
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
            // Check if connection to neurocell is still valid
            NeurocellBlockEntity connectedPod = findConnectedNeurocell();
            
            if (connectedPod == null || !connectedPod.hasRagdoll() || connectedPod.getEntityType().isEmpty()) {
                // Connection lost or neurocell no longer has ragdoll - stop reconstruction
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

    // Search for a neurocell connected via neurolink blocks
    private void checkForAdjacentCloningChamber() {
        if (level == null) return;

        // Use breadth-first search to find connected neurocells through neurolinks
        NeurocellBlockEntity connectedPod = findConnectedNeurocell();
        
        if (connectedPod != null && connectedPod.hasRagdoll() && !connectedPod.getEntityType().isEmpty()) {
            startReconstruction(connectedPod);
        }
    }
    
    // Find a neurocell connected through exactly 1 neurolink block (no direct connection)
    @Nullable
    private NeurocellBlockEntity findConnectedNeurocell() {
        return NeurocellConnector.findConnectedNeurocell(
            level,
            worldPosition,
            this::isValidNeurocellForReformer
        );
    }

    // Check if neurocell is valid for reformer connection
    private boolean isValidNeurocellForReformer(NeurocellBlockEntity neurocell) {
        // Neurocell must have a ragdoll and entity type
        if (!neurocell.hasRagdoll() || neurocell.getEntityType().isEmpty()) {
            return false;
        }

        // Check connection is from left/right sides only (not from back)
        // The reformer connects from the sides, not the back
        // This is validated by checking if the connection is accepted but not from back
        return true; // BlockConnectionHelper already validates acceptsConnectionFrom
    }

    // Start the reconstruction process
    private void startReconstruction(NeurocellBlockEntity neurocell) {
        this.entityType = neurocell.getEntityType();
        this.entityName = neurocell.getEntityName();
        this.reconstructionProgress = 0;
        
        // Get player UUID from neurocell's bioscanner
        net.minecraft.world.item.ItemStack bioscanner = neurocell.getInventory().getItem(0);
        
        if (!bioscanner.isEmpty()) {
            net.minecraft.world.item.component.CustomData customData = bioscanner.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
                net.minecraft.world.item.component.CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            
            if (tag.hasUUID("PlayerUUID")) {
                this.playerUUID = tag.getUUID("PlayerUUID");
            } else {
                this.playerUUID = null;
            }
        }
        
        // Calculate duration based on entity's max health (health × 6 seconds × 20 ticks)
        this.reconstructionDuration = calculateReconstructionDuration(this.entityType);
        
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
        this.playerUUID = null;
        this.reconstructionProgress = 0;
        this.reconstructionDuration = 0;
        this.isReconstructing = false;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
    
    // Calculate reconstruction duration based on entity's max health
    // Formula: health × 6 seconds × 20 ticks per second = health × 120 ticks
    private int calculateReconstructionDuration(String entityTypeString) {
        if (level == null) return 1200; // 60 seconds default

        try {
            ResourceLocation entityId = ResourceLocation.parse(entityTypeString);
            EntityType<?> type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(entityId);

            if (type != null) {
                // Create temporary entity to get its max health
                Entity entity = type.create(level);
                if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                    float maxHealth = livingEntity.getMaxHealth();
                    return (int) (maxHealth * 120);
                }
            }
        } catch (Exception e) {
            // Failed to calculate, use default
        }

        return 1200; // 60 seconds default
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
                // Spawn position on top of the reformer (8 pixels = 0.5 blocks tall)
                BlockPos spawnPos = worldPosition;
                
                // Spawn the entity
                Entity entity = type.create(serverLevel, null, spawnPos, MobSpawnType.SPAWNER, false, false);
                
                if (entity != null) {
                    // If it's a Steve NPC, set the owner name and UUID
                    if (entity instanceof com.modvane.hologenica.entity.SteveNPCEntity steveNPC) {
                        if (!entityName.isEmpty()) {
                            steveNPC.setOwnerName(entityName);
                        }
                        if (playerUUID != null) {
                            steveNPC.setPlayerUUID(playerUUID);
                        }
                    }

                    // Position entity on top of reformer (center of 0.5 block tall reformer)
                    entity.setPos(
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5
                    );
                    serverLevel.addFreshEntity(entity);
                }
            }
        } catch (Exception e) {
            // Failed to spawn entity - silently fail
        }

        // Reset the pod
        entityType = "";
        entityName = "";
        playerUUID = null;
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
        if (playerUUID != null) {
            tag.putUUID("PlayerUUID", playerUUID);
        }
        tag.putInt("ReconstructionProgress", reconstructionProgress);
        tag.putInt("ReconstructionDuration", reconstructionDuration);
        tag.putBoolean("IsReconstructing", isReconstructing);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        entityName = tag.getString("EntityName");
        if (tag.hasUUID("PlayerUUID")) {
            playerUUID = tag.getUUID("PlayerUUID");
        } else {
            playerUUID = null;
        }
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
        if (playerUUID != null) {
            tag.putUUID("PlayerUUID", playerUUID);
        }
        tag.putInt("ReconstructionProgress", reconstructionProgress);
        tag.putInt("ReconstructionDuration", reconstructionDuration);
        tag.putBoolean("IsReconstructing", isReconstructing);
        return tag;
    }
    
    // Getter for player UUID (used by renderer)
    public java.util.UUID getPlayerUUID() {
        return playerUUID;
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

