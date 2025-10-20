package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.block.entity.NeurocellBlockEntity;
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

    // Timing constants
    private static final int IMPRINT_DURATION = 100; // 5 seconds (100 ticks)

    // Position and physics constants
    private static final double ENTITY_CHECK_HEIGHT = 3.0;
    private static final double ENTITY_CHECK_MIN_Y = 0.5;
    private static final double PULL_STRENGTH = 0.1;
    private static final double CENTER_OFFSET = 0.5;

    // Particle effect intervals
    private static final int SCANNING_PARTICLE_INTERVAL = 2; // Every 2 ticks
    private static final int HELIX_PARTICLE_INTERVAL = 3; // Every 3 ticks
    private static final int SOUND_INTERVAL = 10; // Every 10 ticks

    // Sound pitch constants
    private static final float BASE_SOUND_PITCH = 1.5f;
    private static final float SOUND_PITCH_INCREMENT = 0.5f;
    private static final float SOUND_PITCH_DIVISOR = 60.0f;

    // DNA imprinting consequence constants
    private static final int PLAYER_EFFECT_DURATION = 2400; // 2 minutes (2400 ticks)
    private static final float PLAYER_MIN_HEALTH = 2.0f; // 1 heart

    // Particle effect constants
    private static final int COMPLETION_PARTICLE_COUNT = 25;
    private static final int GLOW_PARTICLE_COUNT = 15;
    private static final double PARTICLE_SPREAD_HORIZONTAL = 0.8;
    private static final double PARTICLE_SPREAD_SMALL = 0.5;
    private static final double PARTICLE_VERTICAL_OFFSET = 0.5;
    private static final double PARTICLE_SPEED = 0.05;
    private static final double PARTICLE_SPEED_SLOW = 0.02;
    private static final double SCANNING_PARTICLE_RADIUS = 1.2;
    private static final double SCANNING_PARTICLE_ANGLE_MULTIPLIER = 0.2;
    private static final double HELIX_ANGLE_MULTIPLIER = 0.3;
    private static final double HELIX_RADIUS = 0.4;
    private static final int HELIX_HEIGHT_CYCLE = 40;
    private static final double HELIX_HEIGHT_MULTIPLIER = 0.05;
    private static final double PARTICLE_SPEED_HELIX = 0.1;

    // State variables
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
            worldPosition.getX(), worldPosition.getY() + ENTITY_CHECK_MIN_Y, worldPosition.getZ(),
            worldPosition.getX() + 1, worldPosition.getY() + ENTITY_CHECK_HEIGHT, worldPosition.getZ() + 1
        );
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, checkBox);

        return entities.isEmpty() ? null : entities.get(0);
    }

    // Pull entity towards the center of the imprinter
    private void pullEntityToCenter(LivingEntity entity) {
        double centerX = worldPosition.getX() + CENTER_OFFSET;
        double centerZ = worldPosition.getZ() + CENTER_OFFSET;
        double targetY = worldPosition.getY() + CENTER_OFFSET;

        double dx = centerX - entity.getX();
        double dz = centerZ - entity.getZ();
        double dy = targetY - entity.getY();

        entity.setDeltaMovement(dx * PULL_STRENGTH, dy * PULL_STRENGTH, dz * PULL_STRENGTH);
        entity.hurtMarked = true; // Force position update
    }

    // Process the imprinting of entity DNA
    private void processImprinting(LivingEntity entity, NeurocellBlockEntity neurocell) {
        if (!isImprinting) {
            startImprinting(entity);
        }

        imprintProgress++;
        spawnImprintingEffects(entity);

        if (imprintProgress >= IMPRINT_DURATION) {
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
        if (imprintProgress % SCANNING_PARTICLE_INTERVAL == 0) {
            double angle = (imprintProgress * SCANNING_PARTICLE_ANGLE_MULTIPLIER) % (2 * Math.PI);
            double x = entity.getX() + Math.cos(angle) * SCANNING_PARTICLE_RADIUS;
            double z = entity.getZ() + Math.sin(angle) * SCANNING_PARTICLE_RADIUS;
            double y = entity.getY() + serverLevel.random.nextDouble() * entity.getBbHeight();
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT, x, y, z, 1, 0, 0, 0, PARTICLE_SPEED_HELIX);
        }

        // DNA helix effect with portal particles
        if (imprintProgress % HELIX_PARTICLE_INTERVAL == 0) {
            double helixAngle = (imprintProgress * HELIX_ANGLE_MULTIPLIER) % (2 * Math.PI);
            double x = entity.getX() + Math.cos(helixAngle) * HELIX_RADIUS;
            double z = entity.getZ() + Math.sin(helixAngle) * HELIX_RADIUS;
            double y = worldPosition.getY() + CENTER_OFFSET + (imprintProgress % HELIX_HEIGHT_CYCLE) * HELIX_HEIGHT_MULTIPLIER;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
        }

        // Play ambient scanning sounds
        if (imprintProgress % SOUND_INTERVAL == 0) {
            float pitch = BASE_SOUND_PITCH + (imprintProgress / SOUND_PITCH_DIVISOR) * SOUND_PITCH_INCREMENT;
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

    // Apply debuffs to player after DNA imprinting
    private void applyPlayerConsequences(Player player) {
        // Drop player to 1 heart (2 health)
        float damageAmount = player.getHealth() - PLAYER_MIN_HEALTH;
        if (damageAmount > 0) {
            // Use level damage sources instead of player damage sources to avoid clone immunity bug
            player.hurt(player.level().damageSources().magic(), damageAmount);
        }

        // Apply debuffs for 2 minutes
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.WEAKNESS, PLAYER_EFFECT_DURATION, 1)); // Weakness II
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, PLAYER_EFFECT_DURATION, 1)); // Slowness II
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.HUNGER, PLAYER_EFFECT_DURATION, 2)); // Hunger III
    }

    // Spawn particle effects when imprinting completes
    private void spawnCompletionEffects(LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Gradual upward stream of enchanted hit particles around entity
        for (int i = 0; i < COMPLETION_PARTICLE_COUNT; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD_HORIZONTAL;
            double offsetZ = (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD_HORIZONTAL;
            double offsetY = level.random.nextDouble() * PARTICLE_VERTICAL_OFFSET;
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                1, 0, 0.5, 0, PARTICLE_SPEED
            );
        }

        // Glow particles rising from imprinter position
        for (int i = 0; i < GLOW_PARTICLE_COUNT; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD_SMALL;
            double offsetZ = (level.random.nextDouble() - 0.5) * PARTICLE_SPREAD_SMALL;
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.GLOW,
                worldPosition.getX() + CENTER_OFFSET + offsetX,
                worldPosition.getY() + CENTER_OFFSET,
                worldPosition.getZ() + CENTER_OFFSET + offsetZ,
                1, 0, 0.3, 0, PARTICLE_SPEED_SLOW
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
            // Players clone as Steve NPCs with their actual skin
            return "hologenica:steve_npc";
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
        return (float) imprintProgress / (float) IMPRINT_DURATION;
    }
}
