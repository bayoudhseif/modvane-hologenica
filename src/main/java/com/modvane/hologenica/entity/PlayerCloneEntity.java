package com.modvane.hologenica.entity;

import com.modvane.hologenica.menu.PlayerCloneMenu;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

// Player clone entity - a cloned humanoid that can follow and assist the player
// Extends TamableAnimal to use Minecraft's built-in owner/pet AI system
public class PlayerCloneEntity extends TamableAnimal {

    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID =
        SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Boolean> IS_FOLLOWING =
        SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.BOOLEAN);

    private String ownerName = ""; // Store the name of who was cloned

    public PlayerCloneEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(false, false); // Start untamed
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYER_UUID, Optional.empty());
        builder.define(IS_FOLLOWING, false);
    }
    
    // Set the name of the original player that was cloned
    public void setOwnerName(String name) {
        this.ownerName = name;
        // Update custom name to show just the name (not "Name Clone")
        this.setCustomName(Component.literal(name));
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

    // Set up AI goals using Minecraft's built-in tamable animal goals
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.2D, 2.0F, 5.0F));
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    // Required override for TamableAnimal - clones don't have breeding food
    @Override
    public boolean isFood(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    // Required override for AgeableMob - clones don't breed
    @Override
    @Nullable
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.AgeableMob otherParent) {
        return null;
    }

    // Set up attributes (health, speed, etc.)
    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
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
            (containerId, playerInventory, player) -> new PlayerCloneMenu(containerId, playerInventory, this),
            Component.empty()
        );
    }

    // Toggle follow mode - clone either follows or wanders
    public void toggleFollowMode(Player player) {
        if (!isTame()) {
            // Tame the clone to this player and start following
            setTame(true, true);
            setOwnerUUID(player.getUUID());
            setOrderedToSit(false); // Make sure it starts following immediately
            this.entityData.set(IS_FOLLOWING, true); // Sync to client
        } else {
            // Toggle between following and wandering
            // When ordered to sit, FollowOwnerGoal is disabled and clone will wander
            boolean currentlyFollowing = !isOrderedToSit();
            boolean newFollowingState = !currentlyFollowing; // Toggle it
            setOrderedToSit(!newFollowingState); // If following, don't sit. If not following, sit.
            this.entityData.set(IS_FOLLOWING, newFollowingState); // Sync to client
        }
    }

    // Get following state (synced to client)
    public boolean isFollowing() {
        return this.entityData.get(IS_FOLLOWING);
    }

    // Save data
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); // TamableAnimal handles owner and sitting state
        if (!this.ownerName.isEmpty()) {
            tag.putString("OwnerName", this.ownerName);
        }
        UUID playerUUID = getPlayerUUID();
        if (playerUUID != null) {
            tag.putUUID("PlayerUUID", playerUUID);
        }
        tag.putBoolean("IsFollowing", isFollowing());
    }

    // Load data
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); // TamableAnimal handles owner and sitting state
        if (tag.contains("OwnerName")) {
            this.ownerName = tag.getString("OwnerName");
            // Restore custom name (just the name, not "Name Clone")
            if (!this.ownerName.isEmpty()) {
                this.setCustomName(Component.literal(this.ownerName));
                this.setCustomNameVisible(true);
            }
        }
        if (tag.hasUUID("PlayerUUID")) {
            setPlayerUUID(tag.getUUID("PlayerUUID"));
        }
        if (tag.contains("IsFollowing")) {
            this.entityData.set(IS_FOLLOWING, tag.getBoolean("IsFollowing"));
        }
    }
}

