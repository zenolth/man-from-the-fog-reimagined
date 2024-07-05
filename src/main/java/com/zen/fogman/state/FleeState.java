package com.zen.fogman.state;

import com.zen.fogman.entity.custom.TheManEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FleeState extends AbstractState {
    public FleeState(TheManEntity mob, World world) {
        super(mob, world);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (!this.mob.isLookedAt()) {
            this.mob.discard();
        }
        this.mob.getLookControl().lookAt(target, 30.0f, 30.0f);

        Vec3d direction = new Vec3d(this.mob.getLookControl().getLookX(),this.mob.getLookControl().getLookY(),this.mob.getLookControl().getLookZ())
                .subtract(this.mob.getPos())
                .normalize()
                .rotateY((float) Math.toRadians(90));

        Vec3d movePosition = this.mob.getPos().add(direction);

        this.mob.getMoveControl().moveTo(movePosition.getX(),movePosition.getY(),movePosition.getZ(),1.4);
    }
}
