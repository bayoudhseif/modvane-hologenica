package com.modvane.hologenica.menu;

import com.modvane.hologenica.entity.SteveNPCEntity;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

// Menu for Steve NPC GUI
public class SteveNPCMenu extends AbstractContainerMenu {

    private final SteveNPCEntity npcEntity;

    public SteveNPCMenu(int containerId, Inventory playerInventory, SteveNPCEntity npcEntity) {
        super(HologenicaMenus.STEVE_NPC.get(), containerId);
        this.npcEntity = npcEntity;
    }

    // Handle button clicks from the client screen
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (npcEntity != null) {
            if (buttonId == 0) {
                // Toggle follow mode
                npcEntity.toggleFollowMode(player);
                return true;
            }
        }
        return false;
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

