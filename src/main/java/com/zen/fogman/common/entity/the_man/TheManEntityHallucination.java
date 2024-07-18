package com.zen.fogman.common.entity.the_man;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class TheManEntityHallucination extends TheManEntity {
    public TheManEntityHallucination(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isHallucination() {
        return true;
    }
}
