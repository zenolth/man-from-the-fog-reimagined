package com.zen.fogman.common.entity.the_man.states;

import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManPredicates;
import com.zen.fogman.common.entity.the_man.TheManStatusEffects;
import com.zen.fogman.common.gamerules.ModGamerules;
import com.zen.fogman.common.other.Util;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ChaseState extends AbstractState {

    public static final double LUNGE_COOLDOWN = 30;
    public static final double LUNGE_CHANCE = 0.4;

    public static final double SPIT_COOLDOWN = 15;
    public static final double SPIT_CHANCE = 0.45;

    public static final double HALLUCINATION_COOLDOWN = 60;
    public static final double HALLUCINATION_CHANCE = 0.1;

    private long lungeCooldown = Util.secToTick(LUNGE_COOLDOWN);
    private long spitCooldown = Util.secToTick(SPIT_COOLDOWN);
    private long hallucinationCooldown = Util.secToTick(HALLUCINATION_COOLDOWN);

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

        if (serverWorld.getGameRules().getBoolean(ModGamerules.MAN_GIVE_DARKNESS_EFFECT)) {
            this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.DARKNESS);
            this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.NIGHT_VISION);
        }
        if (serverWorld.getGameRules().getBoolean(ModGamerules.MAN_GIVE_SPEED_EFFECT)) {
            this.mob.addEffectToClosePlayers(serverWorld, TheManStatusEffects.SPEED);
        }

        this.mob.getLookControl().lookAt(target,30f,30f);
        this.mob.moveTo(target,1.0);

        if (--this.lungeCooldown <= 0L) {
            this.lungeCooldown = Util.secToTick(LUNGE_COOLDOWN);
            if (Math.random() < LUNGE_CHANCE) {
                this.mob.setLunging(false);
                this.mob.lunge(target,0.6);
            }
        }

        if (this.mob.distanceTo(target) > 15 && --this.spitCooldown <= 0L) {
            this.spitCooldown = Util.secToTick(SPIT_COOLDOWN);
            if (Math.random() < SPIT_CHANCE) {
                this.mob.spitAt(target);
            }
        }

        if (--this.hallucinationCooldown <= 0L) {
            this.hallucinationCooldown = Util.secToTick(HALLUCINATION_COOLDOWN);
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
