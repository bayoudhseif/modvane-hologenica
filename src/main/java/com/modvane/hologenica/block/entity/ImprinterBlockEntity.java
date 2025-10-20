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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Imprinter - imprints player genetic data into bioscanners in connected neurocells
public class ImprinterBlockEntity extends BlockEntity {
    
    private static final int IMPRINT_DURATION = 100; // 5 seconds (100 ticks)
    private int imprintProgress = 0;
    private boolean isImprinting = false;
    private String playerName = "";

    public ImprinterBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.IMPRINTER.get(), pos, state);
    }
    
    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        // Check for any living entity on top of the imprinter
        AABB checkBox = new AABB(worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(),
                                   worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, checkBox);
        
        if (!entities.isEmpty()) {
            LivingEntity entity = entities.get(0);
            
            // Find a connected neurocell with an empty bioscanner
            NeurocellBlockEntity neurocell = findConnectedNeurocellWithEmptyBioscanner();
            
            if (neurocell != null) {
                // Pull entity towards center and hold in place (for all entities including players)
                double centerX = worldPosition.getX() + 0.5;
                double centerZ = worldPosition.getZ() + 0.5;
                double targetY = worldPosition.getY() + 0.5;
                
                // Gradually pull entity to center
                double pullStrength = 0.1;
                double dx = centerX - entity.getX();
                double dz = centerZ - entity.getZ();
                double dy = targetY - entity.getY();
                
                entity.setDeltaMovement(dx * pullStrength, dy * pullStrength, dz * pullStrength);
                entity.hurtMarked = true; // Force position update
                
                if (!isImprinting) {
                    // Start imprinting
                    isImprinting = true;
                    imprintProgress = 0;
                    playerName = entity.getDisplayName().getString();
                    setChanged();
                }
                
                // Continue imprinting
                imprintProgress++;
                
                // Spawn particles and play sounds during imprinting
                if (level instanceof ServerLevel serverLevel) {
                    // Circular scanning effect with enchantment table particles
                    if (imprintProgress % 2 == 0) {
                        double angle = (imprintProgress * 0.2) % (2 * Math.PI);
                        double radius = 1.2;
                        double x = entity.getX() + Math.cos(angle) * radius;
                        double z = entity.getZ() + Math.sin(angle) * radius;
                        double y = entity.getY() + level.random.nextDouble() * entity.getBbHeight();
                        
                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                            x, y, z, 1, 0, 0, 0, 0.1);
                    }
                    
                    // DNA helix effect with portal particles
                    if (imprintProgress % 3 == 0) {
                        double helixAngle = (imprintProgress * 0.3) % (2 * Math.PI);
                        double helixRadius = 0.4;
                        double helixHeight = (imprintProgress % 40) * 0.05;
                        double x = entity.getX() + Math.cos(helixAngle) * helixRadius;
                        double z = entity.getZ() + Math.sin(helixAngle) * helixRadius;
                        double y = worldPosition.getY() + 0.5 + helixHeight;
                        
                        serverLevel.sendParticles(ParticleTypes.PORTAL,
                            x, y, z, 1, 0, 0, 0, 0);
                    }
                    
                    // Play ambient scanning sounds
                    if (imprintProgress % 10 == 0) {
                        level.playSound(null, worldPosition, 
                            net.minecraft.sounds.SoundEvents.BEACON_AMBIENT, 
                            net.minecraft.sounds.SoundSource.BLOCKS, 
                            0.3f, 1.5f + (imprintProgress / 60.0f) * 0.5f);
                    }
                }
                
                // Complete imprinting
                if (imprintProgress >= IMPRINT_DURATION) {
                    imprintEntityDNA(neurocell, entity);
                    
                    if (entity instanceof Player player) {
                        // Players: Drop to 1 heart and get debuffs for 2 minutes
                        float damageAmount = player.getHealth() - 2.0f; // Leave 1 heart (2 health)
                        if (damageAmount > 0) {
                            player.hurt(player.damageSources().magic(), damageAmount);
                        }
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.WEAKNESS, 2400, 1)); // 2 minutes Weakness II
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 2400, 1)); // 2 minutes Slowness II
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.HUNGER, 2400, 2)); // 2 minutes Hunger III
                    } else {
                        // Non-player entities: Get killed
                        entity.kill();
                    }
                    
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
    
    // Find a neurocell connected through exactly 1 neurolink (same as Reformer)
    private NeurocellBlockEntity findConnectedNeurocellWithEmptyBioscanner() {
        if (level == null) return null;
        
        // Check all 4 horizontal directions for neurolinks
        for (Direction firstDir : Direction.Plane.HORIZONTAL) {
            BlockPos neurolinkPos = worldPosition.relative(firstDir);
            BlockState neurolinkState = level.getBlockState(neurolinkPos);
            
            // Must be a neurolink (no direct connection to neurocell)
            if (neurolinkState.getBlock() instanceof NeurolinkBlock) {
                // Now check all 4 directions from the neurolink for a neurocell
                for (Direction secondDir : Direction.Plane.HORIZONTAL) {
                    BlockPos neurocellPos = neurolinkPos.relative(secondDir);
                    BlockState neurocellState = level.getBlockState(neurocellPos);
                    
                    if (neurocellState.getBlock() instanceof NeurocellBlock) {
                        BlockEntity be = level.getBlockEntity(neurocellPos);
                        if (be instanceof NeurocellBlockEntity neurocell) {
                            // Check if connection is from the back (Imprinter connects via back)
                            if (neurocell.isBackConnection(secondDir.getOpposite()) && hasEmptyBioscanner(neurocell)) {
                                return neurocell;
                            }
                        }
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
    
    // Imprint entity DNA into the bioscanner
    private void imprintEntityDNA(NeurocellBlockEntity neurocell, LivingEntity entity) {
        ItemStack bioscanner = neurocell.getInventory().getItem(0);
        if (bioscanner.isEmpty()) return;
        
        // Determine entity type and name
        String entityTypeString;
        String entityName;
        
        if (entity instanceof Player player) {
            // Players clone as Steve NPCs with their actual skin
            entityTypeString = "hologenica:steve_npc";
            entityName = player.getDisplayName().getString();
        } else {
            // Other entities clone as themselves
            entityTypeString = EntityType.getKey(entity.getType()).toString();
            entityName = entity.hasCustomName() ? 
                entity.getCustomName().getString() : 
                entity.getDisplayName().getString();
        }
        
        // Add entity DNA to bioscanner
        bioscanner.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag tag = customData.copyTag();
            tag.putString("EntityType", entityTypeString);
            tag.putString("EntityName", entityName);
            // Store player UUID for skin rendering
            if (entity instanceof Player player) {
                java.util.UUID uuid = player.getUUID();
                tag.putUUID("PlayerUUID", uuid);
            }
            return CustomData.of(tag);
        });
        
        // Update the neurocell's inventory
        neurocell.getInventory().setItem(0, bioscanner);
        
        // Spawn gradual completion effects and play success sound
        if (level instanceof ServerLevel serverLevel) {
            // Gradual upward stream of particles
            for (int i = 0; i < 25; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
                double offsetY = level.random.nextDouble() * 0.5;
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ,
                    1, 0, 0.5, 0, 0.05);
            }
            
            // Glow particles rising from imprinter to entity
            for (int i = 0; i < 15; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.GLOW,
                    worldPosition.getX() + 0.5 + offsetX, 
                    worldPosition.getY() + 0.5, 
                    worldPosition.getZ() + 0.5 + offsetZ,
                    1, 0, 0.3, 0, 0.02);
            }
            
            // Success sound with echo effect
            level.playSound(null, entity.blockPosition(), 
                net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 
                net.minecraft.sounds.SoundSource.BLOCKS, 
                0.8f, 1.5f);
            level.playSound(null, entity.blockPosition(), 
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, 
                net.minecraft.sounds.SoundSource.BLOCKS, 
                0.6f, 1.8f);
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


