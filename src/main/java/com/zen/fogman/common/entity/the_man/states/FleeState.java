package com.zen.fogman.common.entity.the_man.states;

import com.zen.fogman.common.entity.the_man.TheManEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class FleeState extends AbstractState {
    public FleeState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            this.mob.discard();
            return;
        }

        this.mob.chaseIfTooClose();

        this.mob.getLookControl().lookAt(target,30f,30f);

        Vec3d direction = new Vec3d(this.mob.getLookControl().getLookX() - this.mob.getX(),0,this.mob.getLookControl().getLookZ() - this.mob.getZ())
                .normalize()
                .rotateY((float) Math.toRadians(90))
                .multiply(100);

        this.mob.moveTo(this.mob.getX() + direction.getX(),this.mob.getY() + direction.getY(),this.mob.getZ() + direction.getZ(), 1.4);

        if (!this.mob.isLookedAt()) {
            this.mob.discard();
        }
    }
}
