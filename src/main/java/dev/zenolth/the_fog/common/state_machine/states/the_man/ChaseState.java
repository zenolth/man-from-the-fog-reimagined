package dev.zenolth.the_fog.common.state_machine.states.the_man;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.state_machine.states.AbstractState;
import dev.zenolth.the_fog.common.state_machine.states.TheManState;
import dev.zenolth.the_fog.common.status_effect.ModStatusEffects;
import dev.zenolth.the_fog.common.status_effect.TheManStatusEffects;
import dev.zenolth.the_fog.common.util.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ChaseState extends AbstractState<TheManEntity, TheManState> {
    public static final int SPIT_COOLDOWN = 200;

    public static final int HALLUCINATION_COOLDOWN = 1200;
    public static final float HALLUCINATION_CHANCE = 0.1f;

    private final Timer spitTimer = new Timer(SPIT_COOLDOWN,true,this::doSpit);
    private final Timer hallucinationTimer = new Timer(HALLUCINATION_COOLDOWN,true,this::doHallucinations);
    private final Timer breakBlocksTimer = new Timer(() -> RandomNum.next(5,10),true,this.mob::handleSurroundingBlocks);

    public ChaseState(TheManEntity mob) {
        super(mob);
        this.spitTimer.start();
        this.hallucinationTimer.start();
        this.breakBlocksTimer.start();
    }

    public void doSpit() {
        var target = this.mob.getTarget();
        if (target == null) return;
        if (RandomNum.nextDouble() < MathHelper.clamp(this.mob.getLowHealthPercent(),0.2,0.8)) {
            this.mob.spitAt(target);
        }
    }

    public void doHallucinations() {
        if (RandomNum.nextFloat() < HALLUCINATION_CHANCE) {
            this.mob.spawnHallucinations();
        }
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        this.spitTimer.tick();
        this.hallucinationTimer.tick();
        this.breakBlocksTimer.tick();

        var target = this.mob.getTarget();

        if (target == null) {
            var lastKnownTarget = this.mob.getLastKnownTargetPosition();

            if (lastKnownTarget != null) {
                var distance = lastKnownTarget.distanceTo(this.mob.getPos());
                var direction = this.mob.getPos().subtract(lastKnownTarget).normalize().withAxis(Direction.Axis.Y,0);
                var angle = MathHelper.atan2(direction.z,direction.x);
                var newAngle = angle + Math.toRadians(5);

                var newDirection = new Vec3d(Math.cos(newAngle), 0, Math.sin(newAngle));

                var moveToPos = lastKnownTarget.add(newDirection.multiply(distance));
                this.mob.moveTo(moveToPos,1.4);
            }
            this.spitTimer.pause();
            this.hallucinationTimer.pause();
            this.breakBlocksTimer.pause();
            return;
        }

        if ((this.mob.squaredDistanceTo(target) > this.mob.squaredAttackRange(target) * 1.2f || this.mob.getHealth() < this.mob.getMaxHealth() / 2)) {
            this.spitTimer.resume();
        } else {
            this.spitTimer.pause();
        }
        this.hallucinationTimer.resume();
        this.breakBlocksTimer.resume();

        if (!this.mob.isReal()) {
            var pos = this.mob.getPos();
            this.mob.setPos(pos.getX(),target.getY(),pos.getZ());
        }

        /*if (Util.blockCast(serverWorld,this.mob.getBlockPos(),target.getBlockPos()) != null) {
            System.out.println("see");
        } else {
            System.out.println("no see");
        }*/

        if (target.isDead() && !(WorldHelper.isBloodMoon(serverWorld) || WorldHelper.isSuperBloodMoon(serverWorld))) {
            for (ServerPlayerEntity player : serverWorld.getPlayers(TheManPredicates.TARGET_PREDICATE)) {
                if (player.isInRange(this.mob, TheManEntity.CHASE_DISTANCE)) {
                    if (!player.hasStatusEffect(ModStatusEffects.PARANOIA)) {
                        player.addStatusEffect(new StatusEffectInstance(
                                ModStatusEffects.PARANOIA,
                                TimeHelper.secToTick(120.0),
                                1,
                                false,
                                true
                        ));
                    }
                }
            }
            this.mob.despawn();
            return;
        }

        if (FogMod.CONFIG.statusEffects.giveStatusEffects) {
            if (FogMod.CONFIG.statusEffects.giveDarkness) {
                this.mob.addEffectToTarget(TheManStatusEffects.DARKNESS);
                this.mob.addEffectToTarget(TheManStatusEffects.NIGHT_VISION);
            }
            if (FogMod.CONFIG.statusEffects.giveSpeed && !target.hasStatusEffect(StatusEffects.SLOWNESS)) {
                this.mob.addEffectToTarget(TheManStatusEffects.SPEED);
            }
        }

        //var grid = PathNodeGrid.get(this.mob.getWorld());

        this.mob.getLookControl().lookAt(target,30f,30f);
        var targetPos = target.getPos();

        /*for (var i = 0.6; i > 0; i -= 0.2) {
            var newPos = target.getPos().offset(target.getMovementDirection(),i);
            if (!grid.isSolid(BlockPos.ofFloored(newPos))) {
                targetPos = newPos;
                break;
            }
        }*/

        this.mob.moveTo(targetPos,1.0);

        for (ServerPlayerEntity player : serverWorld.getPlayers(TheManPredicates.TARGET_PREDICATE)) {
            if (player.isInRange(this.mob, TheManEntity.CHASE_DISTANCE)) {
                if (player.isSleeping()) {
                    player.wakeUp();
                }

                HungerManager hungerManager = player.getHungerManager();

                if (hungerManager.getFoodLevel() < FogMod.CONFIG.miscellaneous.chaseHungerCap) {
                    hungerManager.setFoodLevel(FogMod.CONFIG.miscellaneous.chaseHungerCap);
                }

                player.setSprinting(true);
            }
        }
    }
}
