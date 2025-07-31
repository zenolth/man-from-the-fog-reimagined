package dev.zenolth.the_fog.common.state_machine.states.mimic;

import dev.zenolth.the_fog.common.entity.mimic.MimicEntity;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.MimicState;
import dev.zenolth.the_fog.common.util.*;
import net.minecraft.server.world.ServerWorld;

public class WanderState extends AbstractState<MimicEntity, MimicState> {
    public static final int MIN_COOLDOWN_TICKS = 80;
    public static final int MAX_COOLDOWN_TICKS = 120;

    private final Timer actNaturalTimer = new Timer(() -> RandomNum.next(MIN_COOLDOWN_TICKS,MAX_COOLDOWN_TICKS),true,this::actNatural);

    private final Tween lookTween = new Tween(Easings.inOutCubic);

    public WanderState(MimicEntity entity) {
        super(entity);
        this.actNaturalTimer.start();
    }

    private void actNatural() {
        if (RandomNum.nextFloat() < 0.6) {
            this.mob.stopMoving();
            var oldDir = GeometryHelper.calculateDirection(
                    this.mob.getPitch(),
                    this.mob.getYaw()
            );
            var newDir = GeometryHelper.calculateDirection(
                    RandomNum.next(-30f, 30f),
                    (float) (Math.toDegrees(this.mob.getYaw()) + RandomNum.next(45f, 180f) * (RandomNum.nextBoolean() ? -1 : 1))
            );
            this.lookTween.start(oldDir, newDir, RandomNum.next(2f, 5f));
        } else {
            this.lookTween.pause();
            var dir = this.mob.getLookDirection().rotateY((float) Math.toRadians(RandomNum.next(-360f,360f)));
            var moveToPos = this.mob.getPos().add(dir.multiply(RandomNum.next(15.0,60.0)));
            this.mob.moveTo(moveToPos.x,moveToPos.y,moveToPos.z);
        }
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        this.lookTween.tick();
        this.actNaturalTimer.tick();

        if (!this.mob.getMoveControl().isMoving()) {
            this.mob.getLookControl().lookAt(this.mob.getEyePos().add(this.lookTween.getNormalizedState().multiply(10f)));
        }
    }
}
