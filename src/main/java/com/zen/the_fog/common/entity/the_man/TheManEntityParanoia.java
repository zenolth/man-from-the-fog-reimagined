package com.zen.the_fog.common.entity.the_man;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TheManEntityParanoia extends TheManEntityHallucination {
    @Nullable
    private LivingEntity owner;

    public TheManEntityParanoia(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isParanoia() {
        return true;
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return true;
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        return true;
    }

    public void setOwner(@Nullable LivingEntity entity) {
        this.owner = entity;
    }

    @Nullable
    public LivingEntity getOwner() {
        return this.owner;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getOwner();
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return this.getOwner() != null && player.getUuid() == this.getOwner().getUuid();
    }

    @Override
    protected float getSoundVolume() {
        return 0;
    }

    @Override
    public float getLoudSoundVolume() {
        return 0;
    }
}
