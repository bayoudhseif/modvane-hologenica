package com.modvane.hologenica.item;

import com.modvane.hologenica.client.screen.ManualScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Hologenica Manual - opens a GUI showing all mod recipes
public class ManualItem extends Item {

    public ManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            // Open the manual GUI on client side
            Minecraft.getInstance().setScreen(new ManualScreen());
        }
        
        return InteractionResultHolder.success(stack);
    }
}

