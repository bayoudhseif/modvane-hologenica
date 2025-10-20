package com.modvane.hologenica.entity;

import com.modvane.hologenica.menu.SteveNPCMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;

// Steve NPC entity - a simple humanoid entity that looks like Steve
// Can follow a player when toggle is enabled
public class SteveNPCEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> FOLLOWING = 
        SynchedEntityData.defineId(SteveNPCEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = 
        SynchedEntityData.defineId(SteveNPCEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private UUID ownerUUID; // UUID of the player who controls this NPC
    private Player cachedOwner;
    private String ownerName = ""; // Store the name of who was cloned

    public SteveNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FOLLOWING, false);
        builder.define(PLAYER_UUID, Optional.empty());
    }
    
    // Set the name of the original player that was cloned
    public void setOwnerName(String name) {
        this.ownerName = name;
        // Update custom name to show "Clone" after the name
        this.setCustomName(Component.literal(name + " Clone"));
        this.setCustomNameVisible(true);
    }
    
    public void setPlayerUUID(UUID uuid) {
        this.entityData.set(PLAYER_UUID, Optional.ofNullable(uuid));
    }
    
    public UUID getPlayerUUID() {
        return this.entityData.get(PLAYER_UUID).orElse(null);
    }
    
    // Get the stored owner name
    public String getStoredOwnerName() {
        return ownerName;
    }

    // Set up AI goals
    @Override
    protected void registerGoals() {
        // Basic AI - float in water (like all mobs)
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        
        // Follow owner continuously when toggle is on
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.Goal() {
            {
                this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
            }
            
            @Override
            public boolean canUse() {
                // Only check if following is enabled
                if (!isFollowing()) {
                    return false;
                }
                Player owner = getOwner();
                if (owner == null || owner.isSpectator()) {
                    return false;
                }
                // Always active when following is on and owner exists
                return true;
            }
            
            @Override
            public boolean canContinueToUse() {
                // Keep going as long as following is enabled
                if (!isFollowing()) {
                    return false;
                }
                Player owner = getOwner();
                return owner != null && !owner.isSpectator();
            }
            
            @Override
            public void start() {
            }
            
            @Override
            public void tick() {
                Player owner = getOwner();
                if (owner != null && isFollowing()) {
                    // Look at owner
                    SteveNPCEntity.this.getLookControl().setLookAt(owner, 10.0F, SteveNPCEntity.this.getMaxHeadXRot());
                    
                    double distance = SteveNPCEntity.this.distanceToSqr(owner);
                    
                    // If more than 5 blocks away, move to owner
                    if (distance > 25.0D) {
                        SteveNPCEntity.this.getNavigation().moveTo(owner, 1.2);
                    } 
                    // If within 2 blocks, stop moving
                    else if (distance < 4.0D) {
                        SteveNPCEntity.this.getNavigation().stop();
                    }
                    // Between 2-5 blocks, keep slowly moving
                    else {
                        SteveNPCEntity.this.getNavigation().moveTo(owner, 1.0);
                    }
                }
            }
            
            @Override
            public void stop() {
                SteveNPCEntity.this.getNavigation().stop();
            }
        });
        
        // Look at nearby players (like villagers)
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8.0F));
        
        // Random looking (like all passive mobs)
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    // Set up attributes (health, speed, etc.)
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    // Handle right-click to open GUI
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(getMenuProvider());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    // Provide menu for GUI
    private MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
            (containerId, playerInventory, player) -> new SteveNPCMenu(containerId, playerInventory, this),
            Component.empty()
        );
    }

    // Toggle follow mode
    public void toggleFollowMode(Player player) {
        boolean newFollowing = !isFollowing();
        setFollowing(newFollowing);
        
        if (newFollowing) {
            // Set this player as the owner
            this.ownerUUID = player.getUUID();
            this.cachedOwner = player;
        } else {
            // Clear owner
            this.ownerUUID = null;
            this.cachedOwner = null;
        }
    }

    // Get following state
    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    // Set following state
    private void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    // Get the owner player
    public Player getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            this.cachedOwner = serverLevel.getPlayerByUUID(this.ownerUUID);
            return this.cachedOwner;
        }
        return null;
    }

    // Save data
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Following", isFollowing());
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        if (!this.ownerName.isEmpty()) {
            tag.putString("OwnerName", this.ownerName);
        }
        UUID playerUUID = getPlayerUUID();
        if (playerUUID != null) {
            tag.putUUID("PlayerUUID", playerUUID);
        }
    }

    // Load data
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFollowing(tag.getBoolean("Following"));
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("OwnerName")) {
            this.ownerName = tag.getString("OwnerName");
            // Restore custom name
            if (!this.ownerName.isEmpty()) {
                this.setCustomName(Component.literal(this.ownerName + " Clone"));
                this.setCustomNameVisible(true);
            }
        }
        if (tag.hasUUID("PlayerUUID")) {
            setPlayerUUID(tag.getUUID("PlayerUUID"));
        }
    }
}

