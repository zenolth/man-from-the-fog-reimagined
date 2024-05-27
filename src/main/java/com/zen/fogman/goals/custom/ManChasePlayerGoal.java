package com.zen.fogman.goals.custom;

import com.zen.fogman.entity.custom.TheManEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ManChasePlayerGoal
        extends Goal {
    private final TheManEntity man;
    @Nullable
    private LivingEntity target;
    private double speed;

    public ManChasePlayerGoal(TheManEntity man,double speed) {
        this.man = man;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        this.target = this.man.getTarget();
        if (this.target == null) {
            return false;
        }
        double d = this.target.squaredDistanceTo(this.man);
        return !(d > 256.0);
    }

    @Override
    public void start() {
        this.man.getNavigation().startMovingTo(target,speed);
    }

    @Override
    public void stop() {
        //this.man.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }
        this.man.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
    }
}