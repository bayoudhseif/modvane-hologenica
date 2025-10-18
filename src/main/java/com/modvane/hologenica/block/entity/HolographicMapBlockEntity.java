package com.modvane.hologenica.block.entity;

import com.modvane.hologenica.menu.HolographicMapMenu;
import com.modvane.hologenica.registry.HologenicaBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// Stores the map region and cached terrain scan for holographic display
public class HolographicMapBlockEntity extends BlockEntity {

    // Default settings: 16x16 scan, 1x1 display, transparent, rotating
    private static final int DEFAULT_SCAN_SIZE = 16;
    private static final int DEFAULT_BLOCK_SIZE = 1;
    private static final boolean DEFAULT_TRANSPARENT = true;
    private static final boolean DEFAULT_ROTATION = true;
    
    // Scan area bounds (inclusive)
    private int scanMinX, scanMaxX, scanMinZ, scanMaxZ;
    private boolean regionValid = false;
    
    // Configurable settings
    private int scanSize = DEFAULT_SCAN_SIZE;
    private int blockSize = DEFAULT_BLOCK_SIZE;
    private boolean transparentMode = DEFAULT_TRANSPARENT;
    private boolean rotationEnabled = DEFAULT_ROTATION;

    // Cached terrain data to avoid rescanning every frame
    private int[][][] cachedTerrain = null;
    private boolean needsRescan = true;

    public HolographicMapBlockEntity(BlockPos pos, BlockState state) {
        super(HologenicaBlockEntities.HOLOGRAPHIC_MAP.get(), pos, state);
    }

    // Setup scan region based on current scan size
    public void setupScanRegion() {
        BlockPos center = getBlockPos();
        int halfSize = scanSize / 2;
        
        // Calculate exact bounds for the scan size
        scanMinX = center.getX() - halfSize;
        scanMaxX = center.getX() + halfSize - 1; // -1 to get exact scanSize width
        scanMinZ = center.getZ() - halfSize;
        scanMaxZ = center.getZ() + halfSize - 1; // -1 to get exact scanSize depth
        
        // Verify dimensions are correct
        int actualWidth = scanMaxX - scanMinX + 1;
        int actualDepth = scanMaxZ - scanMinZ + 1;
        
        regionValid = (actualWidth == scanSize && actualDepth == scanSize);
        
        // Clear cache and mark for rescan
        cachedTerrain = null;
        needsRescan = true;
        setChanged();
        
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    // Get cached terrain data (null if needs rescan)
    public int[][][] getCachedTerrain() {
        return cachedTerrain;
    }

    // Update the cached terrain data
    public void setCachedTerrain(int[][][] terrain) {
        this.cachedTerrain = terrain;
        this.needsRescan = false;
    }

    // Check if terrain needs to be rescanned
    public boolean needsRescan() {
        return needsRescan;
    }

    // Force a rescan on next render
    public void markForRescan() {
        this.needsRescan = true;
    }

    // Check if hologram is in transparent mode
    public boolean isTransparentMode() {
        return transparentMode;
    }

    // Toggle between transparent and solid rendering
    public void toggleTransparency() {
        this.transparentMode = !this.transparentMode;
        setChanged();
        if (level != null && !level.isClientSide) {
            // Use flag 2 for immediate client update (more efficient than flag 3)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }
    
    // Check if rotation is enabled
    public boolean isRotationEnabled() {
        return rotationEnabled;
    }
    
    // Toggle rotation on/off
    public void toggleRotation() {
        this.rotationEnabled = !this.rotationEnabled;
        setChanged();
        if (level != null && !level.isClientSide) {
            // Use flag 2 for immediate client update (more efficient than flag 3)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }
    
    // Get current scan size
    public int getScanSize() {
        return scanSize;
    }
    
    // Set scan size and update region
    public void setScanSize(int newSize) {
        this.scanSize = newSize;
        setupScanRegion();
    }
    
    // Get current block size
    public int getBlockSize() {
        return blockSize;
    }
    
    // Set block size
    public void setBlockSize(int newSize) {
        this.blockSize = newSize;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    // Get scan bounds
    public int getScanMinX() { return scanMinX; }
    public int getScanMaxX() { return scanMaxX; }
    public int getScanMinZ() { return scanMinZ; }
    public int getScanMaxZ() { return scanMaxZ; }

    // Check if scan region is valid
    public boolean hasValidRegion() {
        return regionValid;
    }

    // Save settings to disk
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ScanSize", scanSize);
        tag.putInt("BlockSize", blockSize);
        tag.putBoolean("TransparentMode", transparentMode);
        tag.putBoolean("RotationEnabled", rotationEnabled);
        tag.putInt("ScanMinX", scanMinX);
        tag.putInt("ScanMaxX", scanMaxX);
        tag.putInt("ScanMinZ", scanMinZ);
        tag.putInt("ScanMaxZ", scanMaxZ);
        tag.putBoolean("RegionValid", regionValid);
    }

    // Load settings from disk
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load scan settings
        if (tag.contains("ScanSize")) {
            scanSize = tag.getInt("ScanSize");
        }
        if (tag.contains("BlockSize")) {
            blockSize = tag.getInt("BlockSize");
        }
        if (tag.contains("TransparentMode")) {
            transparentMode = tag.getBoolean("TransparentMode");
        }
        if (tag.contains("RotationEnabled")) {
            rotationEnabled = tag.getBoolean("RotationEnabled");
        }
        
        // Load bounds if present
        boolean hasBounds = tag.contains("ScanMinX") && tag.contains("ScanMaxX") && 
                           tag.contains("ScanMinZ") && tag.contains("ScanMaxZ");
        
        if (hasBounds) {
            scanMinX = tag.getInt("ScanMinX");
            scanMaxX = tag.getInt("ScanMaxX");
            scanMinZ = tag.getInt("ScanMinZ");
            scanMaxZ = tag.getInt("ScanMaxZ");
            regionValid = tag.getBoolean("RegionValid");
            
            // Verify loaded bounds match the scan size
            int loadedWidth = scanMaxX - scanMinX + 1;
            int loadedDepth = scanMaxZ - scanMinZ + 1;
            
            if (loadedWidth != scanSize || loadedDepth != scanSize) {
                setupScanRegion();
            } else {
                // Bounds are correct but clear cached terrain to ensure rescan with new bounds
                cachedTerrain = null;
                needsRescan = true;
            }
        } else {
            // No bounds in NBT, calculate them
            setupScanRegion();
        }
    }

    // Send data to client for rendering
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("ScanSize", scanSize);
        tag.putInt("BlockSize", blockSize);
        tag.putBoolean("TransparentMode", transparentMode);
        tag.putBoolean("RotationEnabled", rotationEnabled);
        tag.putInt("ScanMinX", scanMinX);
        tag.putInt("ScanMaxX", scanMaxX);
        tag.putInt("ScanMinZ", scanMinZ);
        tag.putInt("ScanMaxZ", scanMaxZ);
        tag.putBoolean("RegionValid", regionValid);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Provide menu for the GUI
    public MenuProvider getMenuProvider() {
        return new SimpleMenuProvider(
            (containerId, playerInventory, player) -> new HolographicMapMenu(containerId, playerInventory, this),
            Component.translatable("block.hologenica.holographic_map")
        );
    }
}
