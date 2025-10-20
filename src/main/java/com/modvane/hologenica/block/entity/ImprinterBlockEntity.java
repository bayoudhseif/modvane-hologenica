package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.block.NeurocellBlock;
import com.modvane.hologenica.block.NeurolinkBlock;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

// Imprinter - imprints player genetic data into bioscanners in connected neurocells
public class ImprinterBlockEntity extends BlockEntity {
    
    private static final int IMPRINT_DURATION = 60; // 3 seconds (60 ticks)
    private int imprintProgress = 0;
    private boolean isImprinting = false;
    private String playerName = "";

    public ImprinterBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.IMPRINTER.get(), pos, state);
    }
    
    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        // Check if a player is standing on top of the imprinter
        AABB checkBox = new AABB(worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(),
                                   worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
        List<Player> players = level.getEntitiesOfClass(Player.class, checkBox);
        
        if (!players.isEmpty()) {
            Player player = players.get(0);
            
            // Find a connected neurocell with an empty bioscanner
            NeurocellBlockEntity neurocell = findConnectedNeurocellWithEmptyBioscanner();
            
            if (neurocell != null) {
                if (!isImprinting) {
                    // Start imprinting
                    isImprinting = true;
                    imprintProgress = 0;
                    playerName = player.getDisplayName().getString();
                    setChanged();
                }
                
                // Continue imprinting
                imprintProgress++;
                
                // Spawn particles around player every few ticks
                if (imprintProgress % 5 == 0 && level instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 3; i++) {
                        double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
                        double offsetY = level.random.nextDouble() * 2;
                        double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
                        serverLevel.sendParticles(ParticleTypes.END_ROD,
                            player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                            1, 0, 0, 0, 0);
                    }
                }
                
                // Complete imprinting
                if (imprintProgress >= IMPRINT_DURATION) {
                    imprintPlayerDNA(neurocell, player);
                    isImprinting = false;
                    imprintProgress = 0;
                    setChanged();
                }
            } else {
                // No valid neurocell - reset
                if (isImprinting) {
                    isImprinting = false;
                    imprintProgress = 0;
                    setChanged();
                }
            }
        } else {
            // No player on imprinter - reset
            if (isImprinting) {
                isImprinting = false;
                imprintProgress = 0;
                setChanged();
            }
        }
    }
    
    // Find a directly connected neurocell with an empty bioscanner (no neurolinks allowed)
    private NeurocellBlockEntity findConnectedNeurocellWithEmptyBioscanner() {
        if (level == null) return null;
        
        // Check all 4 horizontal directions for a DIRECT neurocell connection
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            
            // Must be a neurocell directly adjacent (no neurolinks)
            if (neighborState.getBlock() instanceof NeurocellBlock) {
                BlockEntity be = level.getBlockEntity(neighbor);
                if (be instanceof NeurocellBlockEntity neurocell) {
                    // Check if connection is from the back (Imprinter can only connect via back)
                    if (neurocell.isBackConnection(direction.getOpposite()) && hasEmptyBioscanner(neurocell)) {
                        return neurocell;
                    }
                }
            }
        }
        
        return null;
    }
    
    // Check if neurocell has an empty bioscanner
    private boolean hasEmptyBioscanner(NeurocellBlockEntity neurocell) {
        ItemStack stack = neurocell.getInventory().getItem(0);
        if (stack.isEmpty()) return false;
        if (!stack.getItem().toString().contains("bioscanner")) return false;
        
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return !tag.contains("EntityType");
    }
    
    // Imprint player DNA into the bioscanner
    private void imprintPlayerDNA(NeurocellBlockEntity neurocell, Player player) {
        ItemStack bioscanner = neurocell.getInventory().getItem(0);
        if (bioscanner.isEmpty()) return;
        
        // Add player DNA to bioscanner
        bioscanner.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag tag = customData.copyTag();
            tag.putString("EntityType", "hologenica:steve_npc");
            tag.putString("EntityName", player.getDisplayName().getString());
            return CustomData.of(tag);
        });
        
        // Update the neurocell's inventory
        neurocell.getInventory().setItem(0, bioscanner);
        
        // Spawn success particles
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 2;
                double offsetY = level.random.nextDouble() * 2;
                double offsetZ = (level.random.nextDouble() - 0.5) * 2;
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                    1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ImprintProgress", imprintProgress);
        tag.putBoolean("IsImprinting", isImprinting);
        tag.putString("PlayerName", playerName);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        imprintProgress = tag.getInt("ImprintProgress");
        isImprinting = tag.getBoolean("IsImprinting");
        playerName = tag.getString("PlayerName");
    }

    // Sync data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("ImprintProgress", imprintProgress);
        tag.putBoolean("IsImprinting", isImprinting);
        tag.putString("PlayerName", playerName);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public boolean isImprinting() {
        return isImprinting;
    }
    
    public float getImprintProgress() {
        if (!isImprinting) return 0.0f;
        return (float) imprintProgress / (float) IMPRINT_DURATION;
    }
}

