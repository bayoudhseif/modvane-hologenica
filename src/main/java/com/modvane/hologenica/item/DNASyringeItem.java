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

// Syringe item that captures entity DNA for cloning
public class DNASyringeItem extends Item {

    public DNASyringeItem(Properties properties) {
        super(properties);
    }

    // Right-click on an entity to capture its DNA
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        captureDNA(stack, player, hand, entity);
        return InteractionResult.SUCCESS;
    }

    // Right-click in air to sample yourself
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResultHolder.success(stack);
        }
        
        captureDNA(stack, player, hand, player);
        return net.minecraft.world.InteractionResultHolder.success(stack);
    }

    // Helper method to capture DNA from any living entity
    private void captureDNA(ItemStack stack, Player player, InteractionHand hand, LivingEntity entity) {
        // Store entity type and custom name
        String entityTypeString = EntityType.getKey(entity.getType()).toString();
        String entityName = entity.hasCustomName() ? 
            entity.getCustomName().getString() : 
            entity.getDisplayName().getString();
        
        // Use CUSTOM_DATA to store information
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag tag = customData.copyTag();
            tag.putString("EntityType", entityTypeString);
            tag.putString("EntityName", entityName);
            return CustomData.of(tag);
        });
        
        // Update player's held item to sync to client
        player.setItemInHand(hand, stack);
    }

    // Show what entity is stored in the tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        
        if (tag.contains("EntityType")) {
            String entityName = tag.contains("EntityName") ? tag.getString("EntityName") : tag.getString("EntityType");
            
            tooltip.add(Component.literal("Contains: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(entityName).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.literal("Empty - Right-click an entity").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // Glint effect when DNA is stored
    @Override
    public boolean isFoil(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("EntityType");
    }
}

