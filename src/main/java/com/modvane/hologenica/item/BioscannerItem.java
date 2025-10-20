package com.modvane.hologenica.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

// Bioscanner item that captures entity DNA for cloning
public class BioscannerItem extends Item {

    public BioscannerItem(Properties properties) {
        super(properties);
    }

    // Right-click disabled - use Imprinter to capture DNA


    // Show what entity is stored in the tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        
        if (tag.contains("EntityType")) {
            String entityName = tag.contains("EntityName") ? tag.getString("EntityName") : tag.getString("EntityType");
            tooltip.add(Component.literal(entityName).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("Empty").withStyle(ChatFormatting.GRAY));
        }
    }

    // Glint effect when DNA is stored
    @Override
    public boolean isFoil(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("EntityType");
    }
}

