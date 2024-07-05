package com.zen.fogman.state;

import com.zen.fogman.entity.custom.TheManEntity;
import com.zen.fogman.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ChaseState extends AbstractState {

    private long lastMoveTime;
    private long lastLungeTime;
    private boolean didLunge = false;
    private int cooldown;

    public ChaseState(TheManEntity mob, World world) {
        super(mob, world);
        this.lastMoveTime = MathUtils.tickToSec(this.world.getTime());
        this.lastLungeTime = MathUtils.tickToSec(this.world.getTime());
    }

    public void attack(LivingEntity target, double squaredDistance) {
        double d = this.mob.getSquaredMaxAttackDistance(target);
        if (squaredDistance <= d && this.cooldown <= 0) {
            this.cooldown = MathUtils.toGoalTicks(20);
            this.mob.swingHand(Hand.MAIN_HAND);
            this.mob.attackTarget(target);
        }
    }

    public void attackTick(LivingEntity target) {
        double d = this.mob.getSquaredDistanceToAttackPosOf(target);
        this.cooldown = Math.max(this.cooldown - 1, 0);
        this.attack(target,d);
    }

    public void lungeTick(LivingEntity target) {
        if (this.mob.random2.nextFloat(0f,1f) < 0.25 && !didLunge) {
            didLunge = true;
            this.mob.doLunge(target);
        }

        if (didLunge) {
            if (MathUtils.tickToSec(this.world.getTime()) - this.lastLungeTime > 20) {
                didLunge = false;
                this.lastLungeTime = MathUtils.tickToSec(this.world.getTime());
            }
        } else {
            this.lastLungeTime = MathUtils.tickToSec(this.world.getTime());
        }
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        this.attackTick(target);
        this.lungeTick(target);
        this.mob.breakBlocksAround(serverWorld);
        this.mob.closeTrapdoors(serverWorld);
        this.mob.addEffectsToClosePlayers(serverWorld,this.mob.getPos(),this.mob,TheManEntity.MAN_CHASE_DISTANCE);

        this.mob.getLookControl().lookAt(target, 30.0f, 30.0f);

        if (MathUtils.tickToSec(this.mob.getWorld().getTime()) - this.lastMoveTime > 0.05) {
            this.mob.getNavigation().startMovingTo(target, 1);
            this.lastMoveTime = MathUtils.tickToSec(this.mob.getWorld().getTime());
        }
    }
}
