package com.modvane.hologenica.menu;

import com.modvane.hologenica.entity.SteveNPCEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

// Menu for Steve NPC GUI
public class SteveNPCMenu extends AbstractContainerMenu {

    private final SteveNPCEntity npcEntity;
    private final ContainerData data;

    public SteveNPCMenu(int containerId, Inventory playerInventory, SteveNPCEntity npcEntity) {
        super(HologenicaMenus.STEVE_NPC.get(), containerId);
        this.npcEntity = npcEntity;

        // Create data container to sync following state to client
        this.data = new SimpleContainerData(1);
        addDataSlots(this.data);
    }

    // Handle button clicks from the client screen
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (npcEntity != null && buttonId == 0) {
            // Toggle follow mode
            npcEntity.toggleFollowMode(player);
            return true;
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        // Update the synced data with current following state
        if (npcEntity != null) {
            this.data.set(0, npcEntity.isFollowing() ? 1 : 0);
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
        return npcEntity != null && npcEntity.isAlive() && player.distanceToSqr(npcEntity) <= 64.0;
    }

    public SteveNPCEntity getNpcEntity() {
        return npcEntity;
    }
}

