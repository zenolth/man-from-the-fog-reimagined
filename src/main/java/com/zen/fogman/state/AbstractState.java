package com.zen.fogman.state;

import com.zen.fogman.entity.custom.TheManEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public abstract class AbstractState {
    public TheManEntity mob;
    public World world;

    public AbstractState(TheManEntity entity, World world) {
        this.mob = entity;
        this.world = world;
    }

    public void tick(ServerWorld serverWorld) {

    }
}
