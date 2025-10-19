package com.modvane.hologenica.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

// Steve NPC entity - a simple humanoid entity that looks like Steve
// No AI - just stands there doing nothing
public class SteveNPCEntity extends PathfinderMob {

    public SteveNPCEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    // No AI goals - entity does nothing
    @Override
    protected void registerGoals() {
        // Intentionally empty - no AI behaviors
    }

    // Set up attributes (health, speed, etc.)
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0); // No movement
    }
}

