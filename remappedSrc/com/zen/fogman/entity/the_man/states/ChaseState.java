package com.zen.fogman.common.entity.the_man.states;

import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManPredicates;
import com.zen.fogman.common.entity.the_man.TheManStatusEffects;
import com.zen.fogman.common.other.MathUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ChaseState extends AbstractState {

    public static final double LUNGE_COOLDOWN = 30;
    public static final double LUNGE_CHANCE = 0.4;

    public static final double HALLUCINATION_COOLDOWN = 60;
    public static final double HALLUCINATION_CHANCE = 0.1;

    private long lungeCooldown = MathUtils.secToTick(LUNGE_COOLDOWN);
    private long hallucinationCooldown = MathUtils.secToTick(HALLUCINATION_COOLDOWN);

    public ChaseState(TheManEntity mob) {
        super(mob);
    }

    @Override
    public void tick(ServerWorld serverWorld) {
        LivingEntity target = this.mob.getTarget();

        if (target == null) {
            return;
        }

        this.mob.breakBlocksAround();

        this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.DARKNESS);
        this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.SPEED);
        this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.NIGHT_VISION);

        this.mob.getLookControl().lookAt(target,30f,30f);
        this.mob.moveTo(target,1.0);

        if (--this.lungeCooldown <= 0L) {
            this.lungeCooldown = MathUtils.secToTick(LUNGE_COOLDOWN);
            if (Math.random() < LUNGE_CHANCE) {
                this.mob.lunge(target,0.6);
            }
        }

        if (--this.hallucinationCooldown <= 0L) {
            this.hallucinationCooldown = MathUtils.secToTick(HALLUCINATION_COOLDOWN);
            if (Math.random() < HALLUCINATION_CHANCE) {
                this.mob.spawnHallucinations();
            }
        }

        for (ServerPlayerEntity player : serverWorld.getPlayers(TheManPredicates.TARGET_PREDICATE)) {
            if (player.isInRange(this.mob, TheManEntity.MAN_CHASE_DISTANCE)) {
                if (player.isSleeping()) {
                    player.wakeUp();
                }

                player.getHungerManager().add(1,1);
                player.setSprinting(true);
            }
        }

        this.mob.attack(target);
    }
}
