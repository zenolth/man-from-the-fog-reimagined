package dev.zenolth.the_fog.common.state_machine.states.the_man;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.BlockStateRaycastContext;

public class PrepareLungeState extends AbstractState<TheManEntity, TheManState> {
    public static float LUNGE_RANGE = 20f;

    public PrepareLungeState(TheManEntity entity) {
        super(entity);
    }

    @Override
    public void start() {
        this.mob.stopMoving();
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            this.mob.setState(TheManState.STALK);
            return;
        }

        this.mob.getLookControl().lookAt(target, 30f, 30f);

        if (this.mob.getTarget().isInRange(this.mob,LUNGE_RANGE) && this.mob.canLunge()) {
            var rayResult = serverWorld.raycast(
                    new BlockStateRaycastContext(
                            this.mob.getEyePos(),
                            target.getEyePos(),
                            TheManPredicates.BLOCK_STATE_PREDICATE
                    )
            );

            if (rayResult.getType() == HitResult.Type.MISS) {
                this.mob.lunge(target,0.4f);
            } else {
                this.mob.stopMoving();
            }
        } else {
            this.mob.setState(TheManState.STALK);
        }
    }
}
