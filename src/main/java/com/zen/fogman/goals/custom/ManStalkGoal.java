
package com.zen.fogman.goals.custom;

import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.EnumSet;

public class ManStalkGoal extends Goal {
    protected final TheManEntity mob;
    private final double speed;
    private Path path;
    private long lastMoveTime;

    public ManStalkGoal(TheManEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.lastMoveTime = MathUtils.tickToSec(this.mob.getWorld().getTime());
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP, Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (this.mob.getState() != TheManEntity.ManState.STALK) {
            return false;
        }
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        this.path = this.mob.getNavigation().findPathTo(livingEntity, 0);
        if (this.path != null) {
            return true;
        }
        return this.getSquaredMaxAttackDistance(livingEntity) >= this.mob.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
    }

    @Override
    public boolean shouldContinue() {
        if (this.mob.getState() != TheManEntity.ManState.STALK) {
            return false;
        }
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (!this.mob.isInWalkTargetRange(livingEntity.getBlockPos())) {
            return false;
        }
        return !(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity)livingEntity).isCreative();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
    }

    @Override
    public void stop() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(livingEntity)) {
            this.mob.setTarget(null);
        }
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.getState() != TheManEntity.ManState.STALK) {
            return;
        }
        LivingEntity livingEntity = this.mob.getTarget();

        if (livingEntity == null) {
            return;
        }

        this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);

        if (MathUtils.tickToSec(this.mob.getWorld().getTime()) - this.lastMoveTime > 0.05) {
            this.mob.getNavigation().startMovingTo(livingEntity, this.speed);
            this.lastMoveTime = MathUtils.tickToSec(this.mob.getWorld().getTime());
        }
    }

    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.mob.getWidth() * 2.0f * (this.mob.getWidth() * 2.0f) + entity.getWidth();
    }
}

