package com.zen.fogman.state;

import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class StalkState extends AbstractState {

    private long lastMoveTime;

    public StalkState(TheManEntity mob, World world) {
        super(mob, world);
        this.lastMoveTime = MathUtils.tickToSec(this.world.getTime());
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity livingEntity = this.mob.getTarget();

        if (livingEntity == null) {
            return;
        }

        this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);

        if (MathUtils.tickToSec(this.world.getTime()) - this.lastMoveTime > 0.05) {
            this.mob.getNavigation().startMovingTo(livingEntity, 0.9);
            this.lastMoveTime = MathUtils.tickToSec(this.world.getTime());
        }

        this.mob.chaseIfTooClose(serverWorld);
    }
}
