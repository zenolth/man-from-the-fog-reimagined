package com.zen.fogman.common.entity.the_man;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class TheManEntityHallucination extends TheManEntity {
    public TheManEntityHallucination(EntityType<? extends TheManEntity> entityType, World world) {
        super(entityType, world);

        this.startChase();
    }

    @Override
    public boolean isHallucination() {
        return true;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!this.method_48926().isClient()) {
            if (this.isAlive()) {
                this.setHealth(this.getHealth() - 4f);
            }
        }
    }
}
