package dev.zenolth.the_fog.common.entity;

import net.minecraft.server.world.ServerWorld;

public interface OnSpawnEntity {
    void onSpawn(ServerWorld world);
}
