package com.modvane.hologenica.menu;

import com.modvane.hologenica.entity.PlayerCloneEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

// Menu for Player Clone GUI
public class PlayerCloneMenu extends AbstractContainerMenu {

    private final PlayerCloneEntity cloneEntity;
    private final ContainerData data;

    public PlayerCloneMenu(int containerId, Inventory playerInventory, PlayerCloneEntity cloneEntity) {
        super(HologenicaMenus.PLAYER_CLONE.get(), containerId);
        this.cloneEntity = cloneEntity;

        // Create data container to sync following state to client
        this.data = new SimpleContainerData(1);
        addDataSlots(this.data);
    }

    // Handle button clicks from the client screen
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (cloneEntity != null && buttonId == 0) {
            // Toggle follow mode
            cloneEntity.toggleFollowMode(player);
            return true;
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // Update the synced data with current following state
        if (cloneEntity != null) {
            this.data.set(0, cloneEntity.isFollowing() ? 1 : 0);
        }
    }

    // Get following state from synced data (works on both client and server)
    public boolean isFollowing() {
        return this.data.get(0) == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return cloneEntity != null && cloneEntity.isAlive() && player.distanceToSqr(cloneEntity) <= 64.0;
    }

    public PlayerCloneEntity getCloneEntity() {
        return cloneEntity;
    }
}

