package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.block.NeurocellBlock;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

// Block entity for Neurocell - displays a static ragdoll model when complete
public class NeurocellBlockEntity extends BlockEntity implements MenuProvider {
    
    // Container to hold the bioscanner
    private final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            NeurocellBlockEntity.this.setChanged();
            if (!isLoading) {
                NeurocellBlockEntity.this.onInventoryChanged();
            }
        }
    };
    
    private String entityType = "";
    private String entityName = ""; // Name of the entity being cloned
    private int cloningTime = 0;
    private boolean hasRagdoll = false; // Whether a ragdoll is currently displayed
    private boolean isLoading = false; // Flag to prevent inventory changes during load
    private static final int CLONING_DURATION = 300; // 15 seconds (20 ticks per second)

    public NeurocellBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.NEUROCELL.get(), pos, state);
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
                this.entityName = "";
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
                    String newEntityName = tag.contains("EntityName") ? tag.getString("EntityName") : "";
                    
                    // If entity type changed, restart cloning with new entity
                    if (!newEntityType.equals(entityType)) {
                        this.entityType = newEntityType;
                        this.entityName = newEntityName;
                        this.cloningTime = 0;
                        this.hasRagdoll = true; // Instantly show ragdoll
                        setChanged();

                        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    }
                }
            } else {
                // Bioscanner present but no DNA - clear everything
                if (!entityType.isEmpty() || hasRagdoll) {
                    this.entityType = "";
                    this.entityName = "";
                    this.cloningTime = 0;
                    this.hasRagdoll = false;
                    setChanged();
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
    }

    // Called every tick
    public void tick() {
        // No ticking needed - ragdoll appears instantly when bioscanner is inserted
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
        tag.putString("EntityName", entityName);
        tag.putInt("CloningTime", cloningTime);
        tag.putBoolean("HasRagdoll", hasRagdoll);
        
        // Save inventory using container save method
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        items.set(0, inventory.getItem(0));
        net.minecraft.world.ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        
        // Set loading flag to prevent onInventoryChanged during load
        isLoading = true;
        
        entityType = tag.getString("EntityType");
        entityName = tag.getString("EntityName");
        cloningTime = tag.getInt("CloningTime");
        hasRagdoll = tag.getBoolean("HasRagdoll");
        
        // Load inventory using container load method
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, items, provider);
        inventory.setItem(0, items.get(0));
        
        // Clear loading flag
        isLoading = false;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hologenica.neurocell");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.modvane.hologenica.menu.NeurocellMenu(containerId, playerInventory, this);
    }

    // Sync data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EntityType", entityType);
        tag.putString("EntityName", entityName);
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
    
    public String getEntityName() {
        return entityName;
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
    
    // Neurocell only accepts connections from left and right sides (perpendicular to facing)
    public boolean acceptsConnectionFrom(Direction direction) {
        if (level == null) return false;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof NeurocellBlock)) return false;
        
        Direction facing = state.getValue(NeurocellBlock.FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        
        return direction == left || direction == right;
    }
}

