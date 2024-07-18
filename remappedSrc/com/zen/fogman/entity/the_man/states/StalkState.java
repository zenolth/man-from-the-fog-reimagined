package com.zen.fogman.common.entity.the_man.states;

import com.zen.fogman.common.entity.the_man.TheManEntity;
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

        this.mob.getLookControl().lookAt(target,30f,30f);
        this.mob.moveTo(target,0.8);
    }
}
