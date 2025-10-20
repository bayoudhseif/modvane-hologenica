package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.item.BioscannerItem;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.util.NeurocellConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    private int imprintProgress = 0;
    private boolean isImprinting = false;
    private String playerName = "";

    public ImprinterBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.IMPRINTER.get(), pos, state);
    }

    // Called every tick
    public void tick() {
        if (level == null || level.isClientSide) return;

        LivingEntity entity = findEntityOnImprinter();
        if (entity == null) {
            resetImprinting();
            return;
        }

        NeurocellBlockEntity neurocell = findConnectedNeurocellWithEmptyBioscanner();
        if (neurocell == null) {
            resetImprinting();
            return;
        }

        pullEntityToCenter(entity);
        processImprinting(entity, neurocell);
    }

    // Find any living entity standing on the imprinter
    @Nullable
    private LivingEntity findEntityOnImprinter() {
        if (level == null) return null;

        AABB checkBox = new AABB(
            worldPosition.getX(), worldPosition.getY() + 0.5, worldPosition.getZ(),
            worldPosition.getX() + 1, worldPosition.getY() + 3.0, worldPosition.getZ() + 1
        );
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, checkBox);

        return entities.isEmpty() ? null : entities.get(0);
    }

    // Pull entity towards the center of the imprinter
    private void pullEntityToCenter(LivingEntity entity) {
        double centerX = worldPosition.getX() + 0.5;
        double centerZ = worldPosition.getZ() + 0.5;
        double targetY = worldPosition.getY() + 0.5;

        double dx = centerX - entity.getX();
        double dz = centerZ - entity.getZ();
        double dy = targetY - entity.getY();

        entity.setDeltaMovement(dx * 0.1, dy * 0.1, dz * 0.1);
        entity.hurtMarked = true; // Force position update
    }

    // Process the imprinting of entity DNA
    private void processImprinting(LivingEntity entity, NeurocellBlockEntity neurocell) {
        if (!isImprinting) {
            startImprinting(entity);
        }

        imprintProgress++;
        spawnImprintingEffects(entity);

        if (imprintProgress >= 100) { // 5 seconds
            completeImprinting(entity, neurocell);
        }
    }

    // Start the imprinting process
    private void startImprinting(LivingEntity entity) {
        isImprinting = true;
        imprintProgress = 0;
        playerName = entity.getDisplayName().getString();
        setChanged();
    }

    // Reset imprinting state
    private void resetImprinting() {
        if (isImprinting) {
            isImprinting = false;
            imprintProgress = 0;
            setChanged();
        }
    }

    // Spawn particle effects during imprinting
    private void spawnImprintingEffects(LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Circular scanning effect with enchantment table particles
        if (imprintProgress % 2 == 0) {
            double angle = (imprintProgress * 0.2) % (2 * Math.PI);
            double x = entity.getX() + Math.cos(angle) * 1.2;
            double z = entity.getZ() + Math.sin(angle) * 1.2;
            double y = entity.getY() + serverLevel.random.nextDouble() * entity.getBbHeight();
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, 1, 0, 0, 0, 0.1);
        }

        // DNA helix effect with portal particles
        if (imprintProgress % 3 == 0) {
            double helixAngle = (imprintProgress * 0.3) % (2 * Math.PI);
            double x = entity.getX() + Math.cos(helixAngle) * 0.4;
            double z = entity.getZ() + Math.sin(helixAngle) * 0.4;
            double y = worldPosition.getY() + 0.5 + (imprintProgress % 40) * 0.05;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
        }

        // Play ambient scanning sounds
        if (imprintProgress % 10 == 0) {
            float pitch = 1.5f + (imprintProgress / 60.0f) * 0.5f;
            level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3f, pitch);
        }
    }

    // Complete the imprinting process
    private void completeImprinting(LivingEntity entity, NeurocellBlockEntity neurocell) {
        imprintEntityDNA(neurocell, entity);
        applyImprintingConsequences(entity);
        spawnCompletionEffects(entity);
        resetImprinting();
    }

    // Apply consequences of DNA imprinting to an entity
    private void applyImprintingConsequences(LivingEntity entity) {
        if (entity instanceof Player player) {
            applyPlayerConsequences(player);
        } else {
            // Non-player entities get killed
            entity.kill();
        }
    }

    // Apply consequences to player after DNA imprinting
    private void applyPlayerConsequences(Player player) {
        // Drop player to 1 heart (2 health)
        float damageAmount = player.getHealth() - 2.0f;
        if (damageAmount > 0) {
            // Use level damage sources instead of player damage sources to avoid clone immunity bug
            player.hurt(player.level().damageSources().magic(), damageAmount);
        }
    }

    // Spawn particle effects when imprinting completes
    private void spawnCompletionEffects(LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Gradual upward stream of enchanted hit particles around entity
        for (int i = 0; i < 25; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.5;
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                1, 0, 0.5, 0, 0.05
            );
        }

        // Glow particles rising from imprinter position
        for (int i = 0; i < 15; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.5;
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.GLOW,
                worldPosition.getX() + 0.5 + offsetX,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5 + offsetZ,
                1, 0, 0.3, 0, 0.02
            );
        }

        // Success sound with echo effect
        level.playSound(null, entity.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8f, 1.5f);
        level.playSound(null, entity.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.8f);
    }

    // Find a connected neurocell with an empty bioscanner
    @Nullable
    private NeurocellBlockEntity findConnectedNeurocellWithEmptyBioscanner() {
        return NeurocellConnector.findConnectedNeurocellFromBack(
            level,
            worldPosition,
            this::hasEmptyBioscanner
        );
    }

    // Check if neurocell has an empty bioscanner
    private boolean hasEmptyBioscanner(NeurocellBlockEntity neurocell) {
        ItemStack stack = neurocell.getInventory().getItem(0);
        if (stack.isEmpty()) return false;

        // Use proper type checking instead of string comparison
        if (!(stack.getItem() instanceof BioscannerItem)) return false;

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return !tag.contains("EntityType");
    }

    // Imprint entity DNA into the bioscanner
    private void imprintEntityDNA(NeurocellBlockEntity neurocell, LivingEntity entity) {
        ItemStack bioscanner = neurocell.getInventory().getItem(0);
        if (bioscanner.isEmpty()) return;

        String entityTypeString = getEntityTypeString(entity);
        String entityName = getEntityName(entity);

        // Add entity DNA to bioscanner
        bioscanner.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag tag = customData.copyTag();
            tag.putString("EntityType", entityTypeString);
            tag.putString("EntityName", entityName);

            // Store player UUID for skin rendering
            if (entity instanceof Player player) {
                tag.putUUID("PlayerUUID", player.getUUID());
            }
            return CustomData.of(tag);
        });

        // Update the neurocell's inventory
        neurocell.getInventory().setItem(0, bioscanner);
    }

    // Get entity type string for DNA storage
    private String getEntityTypeString(LivingEntity entity) {
        if (entity instanceof Player) {
            // Players clone as player clones with their actual skin
            return "hologenica:player_clone";
        } else {
            // Other entities clone as themselves
            return EntityType.getKey(entity.getType()).toString();
        }
    }

    // Get entity name for display
    private String getEntityName(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getDisplayName().getString();
        } else {
            return entity.hasCustomName()
                ? entity.getCustomName().getString()
                : entity.getDisplayName().getString();
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
        return (float) imprintProgress / 100.0f;
    }
}
