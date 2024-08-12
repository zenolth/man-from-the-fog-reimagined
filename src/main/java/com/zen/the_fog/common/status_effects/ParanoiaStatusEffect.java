package com.zen.the_fog.common.status_effects;

import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.entity.the_man.TheManEntityParanoia;
import com.zen.the_fog.common.gamerules.ModGamerules;
import com.zen.the_fog.common.other.Util;
import com.zen.the_fog.common.sounds.ModSounds;
import com.zen.the_fog.common.world.dimension.ModDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class ParanoiaStatusEffect extends StatusEffect {
    public static final long COOLDOWN = 5;
    public static final long MIN_RESET_COOLDOWN = 15;
    public static final long MAX_RESET_COOLDOWN = 60;

    public static final double HALLUCINATION_CHANCE = 0.3;

    private final Random random = new Random();
    private long cooldown = Util.secToTick(COOLDOWN);
    private long resetCooldown = Util.secToTick(this.random.nextLong(MIN_RESET_COOLDOWN,MAX_RESET_COOLDOWN));

    private boolean doesEffects = false;

    protected ParanoiaStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 474545);
    }

    public static void spawnHallucination(LivingEntity entity, double x, double y, double z) {
        TheManEntityParanoia entityParanoia = new TheManEntityParanoia(ModEntities.THE_MAN_PARANOIA,entity.getWorld());
        entityParanoia.setOwner(entity);
        entityParanoia.setPos(x,y,z);

        entity.getWorld().spawnEntity(entityParanoia);
    }

    public void spawnHallucinations(LivingEntity entity) {
        Vec3d lookDirection = Util.getRotationVector(0,entity.getYaw(1.0f));

        for (int i = 0; i < 4; i++) {
            TheManEntityParanoia entityParanoia = new TheManEntityParanoia(ModEntities.THE_MAN_PARANOIA,entity.getWorld());
            entityParanoia.setOwner(entity);
            entityParanoia.setPosition(entity.getPos().add(lookDirection.rotateY((float) Math.toRadians(this.random.nextDouble(-45,45))).multiply(5)));

            entity.getWorld().spawnEntity(entityParanoia);
        }
    }

    public static void playSoundAtPosition(ServerPlayerEntity player, SoundEvent event, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        player.networkHandler
                .sendPacket(
                        new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(event), category, x, y, z, volume, pitch, player.getRandom().nextLong())
                );
    }

    public static void playSoundAtPosition(ServerPlayerEntity player, SoundEvent event, SoundCategory category, Vec3d position, float volume, float pitch) {
        playSoundAtPosition(player,event,category, position.getX(), position.getY(), position.getZ(),volume,pitch);
    }

    public void playRandomSound(LivingEntity entity) {
        Vec3d lookDirection = Util.getRotationVector(0,entity.getYaw(1.0f)).multiply(-1);

        if (!entity.getWorld().isClient() && entity instanceof PlayerEntity player) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (Math.random() < 0.5) {
                playSoundAtPosition(serverPlayer,ModSounds.MAN_CREEPY,SoundCategory.MASTER,serverPlayer.getPos().add(lookDirection.rotateY((float) Math.toRadians(this.random.nextDouble(-70,70))).multiply(6)),0.3f,1f);
            } else {
                BlockState steppingBlockState = serverPlayer.getSteppingBlockState();
                if (!steppingBlockState.isSolidBlock(serverPlayer.getServerWorld(),serverPlayer.getSteppingPos())) {
                    return;
                }
                for (int i = 0; i <= 20; i++) {
                    if (i % 4 == 0) {
                        playSoundAtPosition(serverPlayer,steppingBlockState.getSoundGroup().getStepSound(),SoundCategory.MASTER,serverPlayer.getPos().add(lookDirection.rotateY((float) Math.toRadians(this.random.nextDouble(-70,70))).multiply(6)),0.3f,1f);
                    }
                }
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        amplifier += 1;

        if (amplifier > 3) {
            amplifier = 3;
        }

        if (entity.getWorld().getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
            amplifier *= 3;
        }

        if (entity.isSleeping()) {
            entity.wakeUp();
        }

        if (!this.doesEffects && --this.cooldown <= 0) {
            this.doesEffects = true;

            if (Math.random() < HALLUCINATION_CHANCE * amplifier && (Util.isNight(entity.getWorld()) || entity.getWorld().getGameRules().getBoolean(ModGamerules.MAN_CAN_SPAWN_IN_DAY))) {
                this.spawnHallucinations(entity);
            } else {
                this.playRandomSound(entity);
            }

            this.cooldown = Util.secToTick(COOLDOWN / amplifier);
        }

        if (this.doesEffects && --this.resetCooldown <= 0) {
            this.doesEffects = false;
            this.resetCooldown = Util.secToTick(this.random.nextLong(MIN_RESET_COOLDOWN,MAX_RESET_COOLDOWN));
        }
    }
}
