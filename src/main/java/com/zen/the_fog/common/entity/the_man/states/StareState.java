package com.zen.the_fog.common.entity.the_man.states;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManState;
import com.zen.the_fog.common.other.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class StareState extends AbstractState {

    public static final double STARE_COOLDOWN = 4;
    public static final double NO_STARE_COOLDOWN = 12;

    private long stareCooldown = Util.secToTick(STARE_COOLDOWN);
    private long noStareCooldown = Util.secToTick(NO_STARE_COOLDOWN);

    public StareState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        this.mob.chaseIfTooClose();

        this.mob.getLookControl().lookAt(target, 30f, 30f);

        if (this.mob.isLookedAt()) {
            if (--this.stareCooldown <= 0L) {
                switch (this.mob.getRandom().nextBetween(0,2)) {
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
                this.stareCooldown = Util.secToTick(STARE_COOLDOWN);
            }
            this.noStareCooldown = Util.secToTick(NO_STARE_COOLDOWN);
        } else {
            if (--this.noStareCooldown <= 0L) {
                this.mob.setState(TheManState.STALK);
                this.noStareCooldown = Util.secToTick(NO_STARE_COOLDOWN);
            }
            this.stareCooldown = Util.secToTick(STARE_COOLDOWN);
        }
    }
}
