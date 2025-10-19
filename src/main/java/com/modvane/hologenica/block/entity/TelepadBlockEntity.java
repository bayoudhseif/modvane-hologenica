package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.menu.TelepadMenu;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import com.modvane.hologenica.world.TelepadRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// Block entity that stores telepad name and handles cross-dimension teleportation
// Features charging time, cooldowns, sounds, and particle effects
public class TelepadBlockEntity extends BlockEntity implements MenuProvider {

    private String telepadName = "";

    // Teleportation timing constants
    private static final int CHARGE_TIME = 40; // 2 seconds charging time (20 ticks per second)
    private static final int COOLDOWN_AFTER_TELEPORT = 60; // 3 seconds cooldown after arriving
    
    // Track players currently on this telepad with their charge progress
    private final Map<UUID, Integer> chargingPlayers = new HashMap<>();
    
    // Track players who just teleported to prevent immediate re-teleport
    private final Map<UUID, Long> recentArrivals = new HashMap<>();

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

    // Register this telepad in the persistent registry
    private void updateRegistry() {
        if (level instanceof ServerLevel serverLevel) {
            TelepadRegistry registry = TelepadRegistry.get(serverLevel.getServer());
            if (!telepadName.isEmpty()) {
                registry.registerTelepad(telepadName, serverLevel.dimension(), getBlockPos());
            } else {
                registry.unregisterTelepad(serverLevel.dimension(), getBlockPos());
            }
        }
    }

    // Remove this telepad from the registry (called when block is broken, not when unloaded)
    public void removeFromRegistry() {
        if (level instanceof ServerLevel serverLevel) {
            TelepadRegistry registry = TelepadRegistry.get(serverLevel.getServer());
            registry.unregisterTelepad(serverLevel.dimension(), getBlockPos());
        }
    }

    // Called every tick to handle charging players
    public void tick() {
        if (level == null || level.isClientSide) return;
        
        long currentTime = level.getGameTime();
        
        // Clean up old recent arrivals
        recentArrivals.entrySet().removeIf(entry -> currentTime - entry.getValue() > COOLDOWN_AFTER_TELEPORT);
        
        // Process charging players
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : chargingPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            int chargeTime = entry.getValue();
            
            Player player = level.getPlayerByUUID(playerId);
            if (player == null || !isPlayerOnTelepad(player)) {
                // Player left the telepad, reset charging
                toRemove.add(playerId);
                continue;
            }
            
            // Increment charge time
            chargeTime++;
            chargingPlayers.put(playerId, chargeTime);
            
            // Spawn charging particles
            if (chargeTime % 2 == 0) { // Every 2 ticks
                spawnChargingParticles(player);
            }
            
            // Play charging sound periodically
            if (chargeTime == 1) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.5f, 1.5f);
            } else if (chargeTime % 10 == 0 && chargeTime < CHARGE_TIME) {
                level.playSound(null, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3f, 1.8f);
            }
            
            // Check if fully charged
            if (chargeTime >= CHARGE_TIME) {
                executeTeleport((ServerPlayer) player);
                toRemove.add(playerId);
            }
        }
        
        // Clean up completed charges
        toRemove.forEach(chargingPlayers::remove);
    }
    
    // Check if player is standing on this telepad
    private boolean isPlayerOnTelepad(Player player) {
        BlockPos playerPos = player.blockPosition();
        // Check if player is within the telepad bounds (half-slab height)
        return playerPos.equals(worldPosition) || 
               (playerPos.getX() == worldPosition.getX() && 
                playerPos.getZ() == worldPosition.getZ() && 
                Math.abs(playerPos.getY() - worldPosition.getY()) <= 1);
    }
    
    // Start charging teleportation for a player
    public void startCharging(Player player) {
        if (level == null || level.isClientSide || !(player instanceof ServerPlayer)) {
            return;
        }
        
        UUID playerId = player.getUUID();
        
        // Check if player just arrived (cooldown period)
        if (recentArrivals.containsKey(playerId)) {
            long arrivedAt = recentArrivals.get(playerId);
            long currentTime = level.getGameTime();
            if (currentTime - arrivedAt < COOLDOWN_AFTER_TELEPORT) {
                // Still on cooldown, don't start charging
                return;
            }
        }
        
        // Check if telepad has a name set
        if (telepadName.isEmpty()) {
            player.displayClientMessage(Component.literal("Telepad not configured!"), true);
            return;
        }
        
        // Check if already charging
        if (chargingPlayers.containsKey(playerId)) {
            return;
        }
        
        // Start charging
        chargingPlayers.put(playerId, 0);
    }
    
    // Execute the actual teleportation
    private void executeTeleport(ServerPlayer player) {
        if (level == null) return;
        
        // Find all matching telepads across all dimensions
        List<TelepadDestination> destinations = findMatchingTelepads(level.getServer());
        
        if (destinations.isEmpty()) {
            player.displayClientMessage(Component.literal("No destination found: " + telepadName), true);
            return;
        }
        
        // Pick a random destination
        TelepadDestination dest = destinations.get(level.random.nextInt(destinations.size()));
        
        // Spawn departure particles and play sound
        spawnTeleportParticles(player.position());
        level.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.6f, 1.4f);
        
        // Force-load the destination chunk
        dest.level.getChunk(dest.pos);
        
        // Teleport the player
        player.teleportTo(
            dest.level,
            dest.pos.getX() + 0.5,
            dest.pos.getY() + 0.5,
            dest.pos.getZ() + 0.5,
            player.getYRot(),
            player.getXRot()
        );
        
        // Mark player as recently arrived at destination
        if (dest.level.getBlockEntity(dest.pos) instanceof TelepadBlockEntity destTelepad) {
            destTelepad.markPlayerArrival(player.getUUID());
        }
        
        // Spawn arrival particles and play sound at destination
        dest.level.playSound(null, dest.pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.6f, 1.6f);
        spawnArrivalParticlesAt(dest.level, dest.pos);
        
        // Show success message
        player.displayClientMessage(Component.literal("Teleported to: " + telepadName), true);
    }
    
    // Mark that a player just arrived (prevents immediate re-teleport)
    public void markPlayerArrival(UUID playerId) {
        if (level != null) {
            recentArrivals.put(playerId, level.getGameTime());
        }
    }
    
    // Spawn charging particles around player
    private void spawnChargingParticles(Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        
        // Spawn particles in a circle around the player
        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = 0.5 + Math.random() * 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                x + offsetX, y + Math.random(), z + offsetZ,
                1, 0, 0, 0, 0.05
            );
        }
    }
    
    // Spawn teleport particles at a position
    private void spawnTeleportParticles(net.minecraft.world.phys.Vec3 pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        
        // Large burst of particles
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            pos.x, pos.y + 0.5, pos.z,
            50, 0.3, 0.5, 0.3, 0.5
        );
        
        serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            pos.x, pos.y + 0.5, pos.z,
            20, 0.2, 0.3, 0.2, 0.1
        );
    }
    
    // Spawn teleport particles at a block position
    private void spawnArrivalParticlesAt(ServerLevel serverLevel, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            x, y, z,
            50, 0.3, 0.5, 0.3, 0.5
        );
        
        serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            x, y, z,
            20, 0.2, 0.3, 0.2, 0.1
        );
    }

    // Find all telepads with matching names across all dimensions
    private List<TelepadDestination> findMatchingTelepads(@Nullable MinecraftServer server) {
        List<TelepadDestination> destinations = new ArrayList<>();
        if (server == null || telepadName.isEmpty()) return destinations;

        // Get the persistent registry
        TelepadRegistry registry = TelepadRegistry.get(server);
        List<TelepadRegistry.TelepadLocation> locations = registry.findTelepads(telepadName);

        // Convert to destinations, skipping self
        for (TelepadRegistry.TelepadLocation loc : locations) {
            // Skip self
            if (loc.pos().equals(this.getBlockPos()) &&
                level instanceof ServerLevel serverLevel &&
                loc.dimension().equals(serverLevel.dimension())) {
                continue;
            }

            // Get the dimension
            ServerLevel targetLevel = server.getLevel(loc.dimension());
            if (targetLevel != null) {
                destinations.add(new TelepadDestination(targetLevel, loc.pos()));
            }
        }

        return destinations;
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

    // Called when block entity is removed/unloaded
    // DO NOT remove from registry here - that happens in onRemove() when block is actually broken
    @Override
    public void setRemoved() {
        super.setRemoved();
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
