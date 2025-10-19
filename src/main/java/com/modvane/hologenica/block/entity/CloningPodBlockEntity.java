package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Block entity for Cloning Pod - displays a static ragdoll model when complete
public class CloningPodBlockEntity extends BlockEntity implements MenuProvider {
    
    // Container to hold the bioscanner
    private final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            CloningPodBlockEntity.this.setChanged();
            CloningPodBlockEntity.this.onInventoryChanged();
        }
    };
    
    private String entityType = "";
    private int cloningTime = 0;
    private boolean hasRagdoll = false; // Whether a ragdoll is currently displayed
    private static final int CLONING_DURATION = 300; // 15 seconds (20 ticks per second)

    public CloningPodBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.CLONING_POD.get(), pos, state);
    }
    
    public SimpleContainer getInventory() {
        return inventory;
    }

    // Called when inventory changes
    private void onInventoryChanged() {
        if (level == null || level.isClientSide) return;
        
        ItemStack bioscannerStack = inventory.getItem(0);
        
        // If bioscanner was removed completely, clear everything
        if (bioscannerStack.isEmpty()) {
            if (!entityType.isEmpty() || hasRagdoll) {
                this.entityType = "";
                this.cloningTime = 0;
                this.hasRagdoll = false;
                setChanged();
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            return;
        }
        
        // Check if bioscanner has DNA
        if (bioscannerStack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            net.minecraft.world.item.component.CustomData customData = bioscannerStack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            
            if (tag.contains("EntityType")) {
                String newEntityType = tag.getString("EntityType");
                
                // If entity type changed, restart cloning with new entity
                if (!newEntityType.equals(entityType)) {
                    this.entityType = newEntityType;
                    this.cloningTime = 0;
                    this.hasRagdoll = false;
                    setChanged();
                    
                    com.modvane.hologenica.HologenicaMod.LOGGER.info("Cloning Pod starting clone: {}", entityType);
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        } else {
            // Bioscanner present but no DNA - clear everything
            if (!entityType.isEmpty() || hasRagdoll) {
                this.entityType = "";
                this.cloningTime = 0;
                this.hasRagdoll = false;
                setChanged();
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
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
            if (cloningTime % 5 == 0) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }

            // When cloning is complete, display the ragdoll
            if (cloningTime >= CLONING_DURATION) {
                com.modvane.hologenica.HologenicaMod.LOGGER.info("Cloning complete! Displaying ragdoll for: {}", entityType);
                displayRagdoll();
                // Bioscanner keeps its DNA - it's an infinite template!
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
        
        // Save inventory
        if (!inventory.isEmpty()) {
            CompoundTag inventoryTag = new CompoundTag();
            inventory.getItem(0).save(provider, inventoryTag);
            tag.put("Inventory", inventoryTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        entityType = tag.getString("EntityType");
        cloningTime = tag.getInt("CloningTime");
        hasRagdoll = tag.getBoolean("HasRagdoll");
        
        // Load inventory
        if (tag.contains("Inventory")) {
            inventory.setItem(0, ItemStack.parse(provider, tag.getCompound("Inventory")).orElse(ItemStack.EMPTY));
        }
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hologenica.cloning_pod");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.modvane.hologenica.menu.CloningPodMenu(containerId, playerInventory, this);
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

