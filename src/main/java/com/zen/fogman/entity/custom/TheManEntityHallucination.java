package com.zen.fogman.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TheManEntityHallucination extends TheManEntity {
    public TheManEntityHallucination(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
        this.aliveTime = this.random2.nextLong(15,30);
    }

    @Override
    public void addEffectsToClosePlayers(ServerWorld world, Vec3d pos, @Nullable Entity entity, int range) {

    }

    @Override
    public void playChaseSound() {

    }

    @Override
    public boolean tryAttack(Entity target) {
        this.discard();
        return false;
    }
}