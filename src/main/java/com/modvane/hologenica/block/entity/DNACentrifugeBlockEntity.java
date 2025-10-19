package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.menu.DNACentrifugeMenu;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Block entity for DNA Centrifuge - stores syringe and processes DNA
public class DNACentrifugeBlockEntity extends BlockEntity implements MenuProvider {
    
    // Container to hold the DNA syringe
    private final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            DNACentrifugeBlockEntity.this.setChanged();
        }
    };

    private int processingTime = 0;
    private static final int PROCESSING_DURATION = 100; // 5 seconds (20 ticks per second)

    public DNACentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.DNA_CENTRIFUGE.get(), pos, state);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    // Tick to process DNA
    public void tick() {
        if (level == null || level.isClientSide) return;

        // Check if we have a bioscanner with DNA
        if (!inventory.isEmpty() && inventory.getItem(0).has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            processingTime++;

            if (processingTime >= PROCESSING_DURATION) {
                // Processing complete - send DNA to adjacent cloning chamber
                sendDNAToAdjacentChamber();
                processingTime = 0;
            }
        } else {
            processingTime = 0;
        }
    }

    private void sendDNAToAdjacentChamber() {
        if (level == null) return;

        // Get DNA from the syringe
        if (inventory.isEmpty()) return;
        
        net.minecraft.world.item.ItemStack syringe = inventory.getItem(0);
        net.minecraft.world.item.component.CustomData customData = syringe.getOrDefault(
            net.minecraft.core.component.DataComponents.CUSTOM_DATA, 
            net.minecraft.world.item.component.CustomData.EMPTY);
        net.minecraft.nbt.CompoundTag tag = customData.copyTag();
        
        if (!tag.contains("EntityType")) return;
        
        String entityType = tag.getString("EntityType");

        // Check all adjacent positions for a cloning pod
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            if (level.getBlockEntity(adjacentPos) instanceof CloningPodBlockEntity pod) {
                // Transfer DNA to the pod
                pod.receiveDNA(entityType);
                
                // Consume the bioscanner
                inventory.setItem(0, net.minecraft.world.item.ItemStack.EMPTY);
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ProcessingTime", processingTime);
        
        // Save inventory (only if not empty)
        if (!inventory.isEmpty()) {
            CompoundTag inventoryTag = new CompoundTag();
            inventory.getItem(0).save(provider, inventoryTag);
            tag.put("Inventory", inventoryTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        processingTime = tag.getInt("ProcessingTime");
        
        // Load inventory
        if (tag.contains("Inventory")) {
            inventory.setItem(0, net.minecraft.world.item.ItemStack.parse(provider, tag.getCompound("Inventory")).orElse(net.minecraft.world.item.ItemStack.EMPTY));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hologenica.dna_centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DNACentrifugeMenu(containerId, playerInventory, this);
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getProcessingDuration() {
        return PROCESSING_DURATION;
    }
}

