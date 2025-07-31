package dev.zenolth.the_fog.common.state_machine.states.the_man;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;

public class StalkState extends AbstractState<TheManEntity, TheManState> {
    public static float LUNGE_PREPARE_RANGE = 10f;

    public StalkState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        if (this.mob.isLookedAt()) {
            this.mob.chaseIfTooClose(30);
        }

        var dir = Vec3d.fromPolar(0f,target.getYaw());
        var movePos = target.getPos().subtract(dir);

        this.mob.getLookControl().lookAt(target,30f,30f);

        if (this.mob.getTarget().isInRange(this.mob,LUNGE_PREPARE_RANGE)) {
            var rayResult = serverWorld.raycast(
                    new BlockStateRaycastContext(
                            this.mob.getEyePos(),
                            target.getEyePos(),
                            TheManPredicates.BLOCK_STATE_PREDICATE
                    )
            );
            if (rayResult.getType() == HitResult.Type.BLOCK && this.mob.canLunge()) {
                this.mob.stopMoving();
                this.mob.setState(TheManState.PREPARE_LUNGE);
            } else {
                this.mob.moveTo(movePos,0.8);
            }
        } else {
            this.mob.moveTo(movePos,0.8);
        }
    }
}
