package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Block entity for Cloning Chamber - receives DNA and spawns entities
public class CloningChamberBlockEntity extends BlockEntity {
    
    private String entityType = "";
    private int cloningTime = 0;
    private static final int CLONING_DURATION = 100; // 5 seconds (20 ticks per second)

    public CloningChamberBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.CLONING_CHAMBER.get(), pos, state);
    }

    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;

        // If we have DNA to clone, start the cloning process
        if (!entityType.isEmpty() && cloningTime < CLONING_DURATION) {
            cloningTime++;
            setChanged();

            // When cloning is complete, spawn the entity
            if (cloningTime >= CLONING_DURATION) {
                spawnClonedEntity();
            }
        }
    }

    // Receive DNA from centrifuge
    public void receiveDNA(String entityTypeString) {
        if (entityType.isEmpty() || cloningTime >= CLONING_DURATION) {
            this.entityType = entityTypeString;
            this.cloningTime = 0;
            setChanged();
        }
    }

    // Spawn the cloned entity
    private void spawnClonedEntity() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;
        if (entityType.isEmpty()) return;

        try {
            // Parse the entity type
            ResourceLocation entityId = ResourceLocation.parse(entityType);
            EntityType<?> type = EntityType.byString(entityId.toString()).orElse(null);
            
            if (type != null) {
                // Spawn position above the chamber
                BlockPos spawnPos = worldPosition.above();
                
                // Make sure the spawn position is safe
                if (!serverLevel.getBlockState(spawnPos).isAir()) {
                    spawnPos = worldPosition.above(2);
                }
                
                // Spawn the entity as a baby
                Entity entity = type.create(serverLevel, null, spawnPos, MobSpawnType.SPAWNER, false, false);
                
                if (entity != null) {
                    // Position it in the center of the block
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                    
                    // Make it a baby if possible
                    if (entity instanceof net.minecraft.world.entity.AgeableMob ageableMob) {
                        ageableMob.setBaby(true);
                    }
                    
                    serverLevel.addFreshEntity(entity);
                }
            }
        } catch (Exception e) {
            // Failed to spawn entity - silently fail
        }

        // Reset the chamber
        entityType = "";
        cloningTime = 0;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putString("EntityType", entityType);
        tag.putInt("CloningTime", cloningTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        cloningTime = tag.getInt("CloningTime");
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
        return !entityType.isEmpty() && cloningTime < CLONING_DURATION;
    }
}

