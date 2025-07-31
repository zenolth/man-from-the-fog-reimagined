package dev.zenolth.the_fog.common.state_machine.states.the_man;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import dev.zenolth.the_fog.common.util.RandomNum;
import dev.zenolth.the_fog.common.util.TimeHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class StareState extends AbstractState<TheManEntity, TheManState> {

    public static final double STARE_COOLDOWN = 4;
    public static final double NO_STARE_COOLDOWN = 12;

    private long stareCooldown = TimeHelper.secToTick(STARE_COOLDOWN);
    private long noStareCooldown = TimeHelper.secToTick(NO_STARE_COOLDOWN);

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
                switch (RandomNum.next(0,2)) {
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
                this.stareCooldown = TimeHelper.secToTick(STARE_COOLDOWN);
            }
            this.noStareCooldown = TimeHelper.secToTick(NO_STARE_COOLDOWN);
        } else {
            if (--this.noStareCooldown <= 0L) {
                this.mob.setState(TheManState.STALK);
                this.noStareCooldown = TimeHelper.secToTick(NO_STARE_COOLDOWN);
            }
            this.stareCooldown = TimeHelper.secToTick(STARE_COOLDOWN);
        }
    }
}
