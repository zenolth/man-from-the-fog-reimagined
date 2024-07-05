package com.zen.fogman.state;

import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class StareState extends AbstractState {

    private long stareTime;

    public StareState(TheManEntity mob, World world) {
        super(mob, world);
        this.stareTime = MathUtils.tickToSec(this.world.getTime());
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity livingEntity = this.mob.getTarget();

        if (livingEntity == null) {
            return;
        }

        if (this.mob.isLookedAt()) {
            if (MathUtils.tickToSec(this.world.getTime()) - this.stareTime > TheManEntity.MAN_LOOK_TIME_TO_CHASE) {
                switch (this.mob.random2.nextInt(1,4)) {
                    case 1:
                        this.mob.startChase(serverWorld);
                        break;
                    case 2:
                        this.mob.begone(serverWorld);
                        break;
                    case 3:
                        this.mob.flee();
                        break;
                }
                return;
            }
        } else {
            this.stareTime = MathUtils.tickToSec(this.world.getTime());
        }

        this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);

        this.mob.chaseIfTooClose(serverWorld);
    }
}
