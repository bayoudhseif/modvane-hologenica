package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.TelepadMenu;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Block entity that stores telepad name and handles cross-dimension teleportation
public class TelepadBlockEntity extends BlockEntity implements MenuProvider {

    private String telepadName = "";

    // Global registry of all telepads (dimension + pos -> name)
    private static final Map<String, TelepadInfo> TELEPAD_REGISTRY = new HashMap<>();

    // Cooldown tracking per player to prevent spam teleporting
    private static final Map<String, Long> PLAYER_COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_TICKS = 60; // 3 seconds (20 ticks per second)

    public TelepadBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.TELEPAD.get(), pos, state);
    }

    // Get the telepad name
    public String getTelepadName() {
        return telepadName;
    }

    // Set the telepad name
    public void setTelepadName(String name) {
        this.telepadName = name;
        setChanged();
        if (level != null && !level.isClientSide) {
            updateRegistry();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    // Register this telepad in the global registry
    private void updateRegistry() {
        if (level instanceof ServerLevel serverLevel) {
            String key = getRegistryKey(serverLevel.dimension(), getBlockPos());
            if (!telepadName.isEmpty()) {
                TELEPAD_REGISTRY.put(key, new TelepadInfo(telepadName, serverLevel.dimension(), getBlockPos()));
            } else {
                TELEPAD_REGISTRY.remove(key);
            }
        }
    }

    // Remove this telepad from the registry
    private void removeFromRegistry() {
        if (level instanceof ServerLevel serverLevel) {
            String key = getRegistryKey(serverLevel.dimension(), getBlockPos());
            TELEPAD_REGISTRY.remove(key);
        }
    }

    // Generate a unique key for the registry
    private static String getRegistryKey(ResourceKey<Level> dimension, BlockPos pos) {
        return dimension.location().toString() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    // Teleport an entity to a matching telepad
    public void teleportEntity(Player player) {
        if (level == null || level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Check if telepad has a name set
        if (telepadName.isEmpty()) {
            player.displayClientMessage(Component.literal("Telepad not configured! Right-click to set a name."), true);
            return;
        }

        // Check cooldown
        String playerKey = player.getUUID().toString();
        long currentTick = level.getGameTime();
        if (PLAYER_COOLDOWNS.containsKey(playerKey)) {
            long lastTeleport = PLAYER_COOLDOWNS.get(playerKey);
            if (currentTick - lastTeleport < COOLDOWN_TICKS) {
                return; // Still on cooldown, silently ignore
            }
        }

        // Find all matching telepads across all dimensions
        List<TelepadDestination> destinations = findMatchingTelepads(level.getServer());

        if (destinations.isEmpty()) {
            player.displayClientMessage(Component.literal("No destination telepad found with name: " + telepadName), true);
            return;
        }

        // Pick a random destination (or cycle through them if multiple exist)
        TelepadDestination dest = destinations.get(level.random.nextInt(destinations.size()));

        // Spawn disintegration particles at source
        spawnTeleportParticles((ServerLevel) level, player.position(), true);

        // Play teleport sound at source
        level.playSound(null, getBlockPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Teleport the player
        serverPlayer.teleportTo(
            dest.level,
            dest.pos.getX() + 0.5,
            dest.pos.getY() + 1.0,
            dest.pos.getZ() + 0.5,
            serverPlayer.getYRot(),
            serverPlayer.getXRot()
        );

        // Spawn reintegration particles at destination
        spawnTeleportParticles(dest.level, serverPlayer.position(), false);

        // Play teleport sound at destination
        dest.level.playSound(null, dest.pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.2F);

        // Set cooldown
        PLAYER_COOLDOWNS.put(playerKey, currentTick);

        // Show success message
        player.displayClientMessage(Component.literal("Teleported to: " + telepadName), true);
    }

    // Find all telepads with matching names across all dimensions
    private List<TelepadDestination> findMatchingTelepads(@Nullable MinecraftServer server) {
        List<TelepadDestination> destinations = new ArrayList<>();
        if (server == null || telepadName.isEmpty()) return destinations;

        // Search through the registry for matching names
        for (TelepadInfo info : TELEPAD_REGISTRY.values()) {
            // Skip self
            if (info.pos.equals(this.getBlockPos()) && info.dimension.equals(((ServerLevel) level).dimension())) {
                continue;
            }
            // Check if names match
            if (info.name.equals(this.telepadName)) {
                ServerLevel targetLevel = server.getLevel(info.dimension);
                if (targetLevel != null) {
                    destinations.add(new TelepadDestination(targetLevel, info.pos));
                }
            }
        }

        return destinations;
    }

    // Spawn futuristic disintegration/reintegration particles
    private void spawnTeleportParticles(ServerLevel level, net.minecraft.world.phys.Vec3 pos, boolean isDisintegration) {
        int particleCount = isDisintegration ? 50 : 30;

        for (int i = 0; i < particleCount; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            double velocityX = (level.random.nextDouble() - 0.5) * 0.2;
            double velocityY = isDisintegration ? 0.1 : -0.1; // Rise up when disintegrating, fall when reintegrating
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.2;

            // Mix of portal and end rod particles for futuristic effect
            if (i % 2 == 0) {
                level.sendParticles(
                    ParticleTypes.PORTAL,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    velocityX,
                    velocityY,
                    velocityZ,
                    0.1
                );
            } else {
                level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    velocityX,
                    velocityY,
                    velocityZ,
                    0.05
                );
            }
        }
    }

    // Save telepad name to disk
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("TelepadName", telepadName);
    }

    // Load telepad name from disk
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TelepadName")) {
            telepadName = tag.getString("TelepadName");
        }
    }

    // Called when the block entity is loaded into the world
    @Override
    public void onLoad() {
        super.onLoad();
        // Register this telepad when it loads into the world
        if (level != null && !level.isClientSide) {
            updateRegistry();
        }
    }

    // Called when block entity is removed
    @Override
    public void setRemoved() {
        super.setRemoved();
        removeFromRegistry();
    }

    // Send data to client for synchronization
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("TelepadName", telepadName);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hologenica.telepad");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TelepadMenu(containerId, playerInventory, this);
    }

    // Writer for extra menu data (sends position to client)
    public void writeExtraData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(getBlockPos());
    }

    // Helper record to store telepad information in the registry
    private record TelepadInfo(String name, ResourceKey<Level> dimension, BlockPos pos) {}

    // Helper class to store destination information
    private static class TelepadDestination {
        final ServerLevel level;
        final BlockPos pos;

        TelepadDestination(ServerLevel level, BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }
    }
}
