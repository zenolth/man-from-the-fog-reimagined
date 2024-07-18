package com.zen.fogman.common.entity.the_man.states;

import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManState;
import com.zen.fogman.common.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class StareState extends AbstractState {

    public static final double STARE_COOLDOWN = 4;

    private long stareCooldown = MathUtils.secToTick(STARE_COOLDOWN);

    public StareState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        this.mob.getLookControl().lookAt(target, 30f, 30f);

        if (this.mob.isLookedAt()) {
            if (--this.stareCooldown == 0L) {
                switch (this.mob.getRandom().nextBetween(0,3)) {
                    case 0:
                        this.mob.despawn();
                        break;
                    case 1:
                        this.mob.startChase();
                        break;
                    case 2:
                        this.mob.setState(TheManState.FLEE);
                        break;
                }
                this.stareCooldown = MathUtils.secToTick(STARE_COOLDOWN);
            }
        } else {
            this.stareCooldown = MathUtils.secToTick(STARE_COOLDOWN);
        }
    }
}
