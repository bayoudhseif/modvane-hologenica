package com.modvane.hologenica.world;

import com.modvane.hologenica.HologenicaMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.Registries;

import java.util.*;

// Persistent storage for telepad locations across all dimensions
public class TelepadRegistry extends SavedData {

    private static final String DATA_NAME = HologenicaMod.MODID + "_telepad_registry";

    // Map of telepad name -> list of locations (dimension + position)
    private final Map<String, List<TelepadLocation>> telepads = new HashMap<>();

    public TelepadRegistry() {
    }

    // Load telepad registry from NBT
    public TelepadRegistry(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = tag.getList("Telepads", Tag.TAG_COMPOUND);
        HologenicaMod.LOGGER.info("Loading TelepadRegistry with {} entries", list.size());

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String name = entry.getString("Name");

            ListTag locations = entry.getList("Locations", Tag.TAG_COMPOUND);
            List<TelepadLocation> locationList = new ArrayList<>();

            for (int j = 0; j < locations.size(); j++) {
                CompoundTag locTag = locations.getCompound(j);
                String dimensionStr = locTag.getString("Dimension");
                ResourceLocation dimRL = ResourceLocation.parse(dimensionStr);
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimRL);

                BlockPos pos = new BlockPos(
                    locTag.getInt("X"),
                    locTag.getInt("Y"),
                    locTag.getInt("Z")
                );

                locationList.add(new TelepadLocation(dimension, pos));
                HologenicaMod.LOGGER.info("Loaded telepad '{}' at {} in {}", name, pos, dimensionStr);
            }

            telepads.put(name, locationList);
        }
    }

    // Save telepad registry to NBT
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        HologenicaMod.LOGGER.info("Saving TelepadRegistry with {} telepad names", telepads.size());

        for (Map.Entry<String, List<TelepadLocation>> entry : telepads.entrySet()) {
            CompoundTag telepadEntry = new CompoundTag();
            telepadEntry.putString("Name", entry.getKey());

            ListTag locations = new ListTag();
            for (TelepadLocation loc : entry.getValue()) {
                CompoundTag locTag = new CompoundTag();
                locTag.putString("Dimension", loc.dimension().location().toString());
                locTag.putInt("X", loc.pos().getX());
                locTag.putInt("Y", loc.pos().getY());
                locTag.putInt("Z", loc.pos().getZ());
                locations.add(locTag);
                HologenicaMod.LOGGER.info("Saving telepad '{}' at {} in {}", entry.getKey(), loc.pos(), loc.dimension().location());
            }

            telepadEntry.put("Locations", locations);
            list.add(telepadEntry);
        }

        tag.put("Telepads", list);
        return tag;
    }

    // Get the telepad registry for the server
    public static TelepadRegistry get(MinecraftServer server) {
        TelepadRegistry registry = server.overworld().getDataStorage()
            .computeIfAbsent(
                new SavedData.Factory<>(
                    TelepadRegistry::new,
                    TelepadRegistry::new,
                    null
                ),
                DATA_NAME
            );
        HologenicaMod.LOGGER.info("TelepadRegistry.get() called, registry has {} telepad names", registry.telepads.size());
        return registry;
    }

    // Register a telepad with a name at a specific location
    public void registerTelepad(String name, ResourceKey<Level> dimension, BlockPos pos) {
        if (name.isEmpty()) return;

        telepads.computeIfAbsent(name, k -> new ArrayList<>());
        TelepadLocation newLoc = new TelepadLocation(dimension, pos);

        // Remove any existing entry for this exact position
        List<TelepadLocation> locations = telepads.get(name);
        locations.removeIf(loc -> loc.dimension().equals(dimension) && loc.pos().equals(pos));

        // Add the new location
        locations.add(newLoc);
        HologenicaMod.LOGGER.info("Registered telepad '{}' at {} in {}", name, pos, dimension.location());
        setDirty();
    }

    // Unregister a telepad at a specific location
    public void unregisterTelepad(ResourceKey<Level> dimension, BlockPos pos) {
        for (List<TelepadLocation> locations : telepads.values()) {
            locations.removeIf(loc -> loc.dimension().equals(dimension) && loc.pos().equals(pos));
        }
        // Remove empty entries
        telepads.values().removeIf(List::isEmpty);
        setDirty();
    }

    // Find all locations with matching telepad name
    public List<TelepadLocation> findTelepads(String name) {
        return telepads.getOrDefault(name, Collections.emptyList());
    }

    // Helper record to store telepad location
    public record TelepadLocation(ResourceKey<Level> dimension, BlockPos pos) {}
}
