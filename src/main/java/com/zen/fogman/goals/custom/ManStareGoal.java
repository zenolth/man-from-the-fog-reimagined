
package com.zen.fogman.goals.custom;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.EnumSet;

public class ManStareGoal extends Goal {
    protected final TheManEntity mob;

    private boolean wasLooking = false;
    private long stareTime;

    public ManStareGoal(TheManEntity mob) {
        this.mob = mob;
        this.stareTime = MathUtils.tickToSec(this.mob.getWorld().getTime());
        this.setControls(EnumSet.of(Control.LOOK, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        return this.mob.getState() == TheManEntity.ManState.STARE;
    }

    @Override
    public boolean shouldContinue() {
        return this.mob.getState() == TheManEntity.ManState.STARE;
    }

    @Override
    public void stop() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
            //this.mob.setTarget(null);
        }
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.getState() != TheManEntity.ManState.STARE) {
            return;
        }
        LivingEntity livingEntity = this.mob.getTarget();

        if (livingEntity == null) {
            return;
        }

        if (this.mob.isLookedAt() && !this.wasLooking) {
            this.wasLooking = true;
        }

        if (this.wasLooking && !this.mob.isLookedAt()) {
            this.mob.discard();
        }

        if (this.mob.isLookedAt()) {
            if (MathUtils.tickToSec(this.mob.getWorld().getTime()) - this.stareTime > 7.0) {
                this.mob.updateState(TheManEntity.ManState.CHASE);
            }
        } else {
            this.stareTime = MathUtils.tickToSec(this.mob.getWorld().getTime());
        }

        this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
    }
}

