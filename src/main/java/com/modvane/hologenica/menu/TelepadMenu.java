package com.modvane.hologenica.menu;

import com.modvane.hologenica.block.entity.TelepadBlockEntity;
import com.modvane.hologenica.registry.HologenicaBlocks;
import com.modvane.hologenica.registry.HologenicaMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

// Menu for the telepad block GUI
// Provides access to the block entity for the screen
public class TelepadMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final Player player;

    // Server-side constructor with block entity
    public TelepadMenu(int containerId, Inventory playerInventory, TelepadBlockEntity blockEntity) {
        super(HologenicaMenus.TELEPAD.get(), containerId);
        this.player = playerInventory.player;
        if (blockEntity != null) {
            this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
            this.pos = blockEntity.getBlockPos();
        } else {
            this.access = ContainerLevelAccess.NULL;
            this.pos = BlockPos.ZERO;
        }
    }

    // Client-side constructor (no block entity available)
    public TelepadMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(HologenicaMenus.TELEPAD.get(), containerId);
        this.player = playerInventory.player;
        this.access = ContainerLevelAccess.NULL;
        this.pos = pos;
    }

    // Check if the player can still access this menu
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, HologenicaBlocks.TELEPAD.get());
    }

    // Get the block entity (works on both client and server)
    public TelepadBlockEntity getBlockEntity() {
        if (player != null && player.level() != null) {
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof TelepadBlockEntity telepad) {
                return telepad;
            }
        }
        return null;
    }

    // Get the position for packet sending
    public BlockPos getPos() {
        return pos;
    }

    // Required by AbstractContainerMenu but not used for this GUI
    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
