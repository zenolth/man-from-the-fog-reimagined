package com.zen.the_fog.common.entity.the_man.states;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class StalkState extends AbstractState {
    public StalkState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        if (this.mob.isLookedAt()) {
            this.mob.chaseIfTooClose(30);
        }

        this.mob.getLookControl().lookAt(target,30f,30f);
        this.mob.moveTo(target.getPos(),0.8);
    }
}
