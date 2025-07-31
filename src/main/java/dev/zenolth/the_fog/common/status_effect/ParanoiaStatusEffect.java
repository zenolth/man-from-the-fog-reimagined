package dev.zenolth.the_fog.common.status_effect;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntityParanoia;
import dev.zenolth.the_fog.common.util.*;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.world.dimension.ModDimensions;
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

import java.util.Optional;
import java.util.Random;

public class ParanoiaStatusEffect extends StatusEffect {
    public static final long COOLDOWN = 5;
    public static final long MIN_RESET_COOLDOWN = 15;
    public static final long MAX_RESET_COOLDOWN = 60;

    public static final double HALLUCINATION_CHANCE = 0.3;

    private final Random random = new Random();
    private long cooldown = TimeHelper.secToTick(COOLDOWN);
    private long resetCooldown = TimeHelper.secToTick(this.random.nextLong(MIN_RESET_COOLDOWN,MAX_RESET_COOLDOWN));

    private boolean doesEffects = false;

    protected ParanoiaStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 474545);
    }

    public static void spawnHallucination(PlayerEntity player, double x, double y, double z) {
        var entityParanoia = new TheManEntityParanoia(ModEntities.THE_MAN_PARANOIA,player.getWorld());
        entityParanoia.owner.set(Optional.of(player.getUuid()));
        entityParanoia.setPos(x,y,z);
        player.getWorld().spawnEntity(entityParanoia);
    }

    public static void spawnHallucination(PlayerEntity player, Vec3d pos) {
        spawnHallucination(player,pos.x,pos.y,pos.z);
    }

    public void spawnHallucinations(PlayerEntity player) {
        var lookDirection = GeometryHelper.calculateDirection(0,player.getYaw(1.0f));
        for (int i = 0; i < 4; i++) {
            spawnHallucination(player,player.getPos().add(lookDirection.rotateY((float) Math.toRadians(this.random.nextDouble(-45,45))).multiply(5)));
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
        Vec3d lookDirection = GeometryHelper.calculateDirection(0,entity.getYaw(1.0f)).multiply(-1);

        if (!entity.getWorld().isClient() && entity instanceof PlayerEntity player) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (RandomNum.nextDouble() < 0.5) {
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

            if (entity instanceof PlayerEntity player) {
                if (RandomNum.nextDouble() < HALLUCINATION_CHANCE * amplifier && (WorldHelper.isNight(entity.getWorld()) || FogMod.CONFIG.spawning.spawnInDay)) {
                    this.spawnHallucinations(player);
                } else {
                    this.playRandomSound(player);
                }
            }

            this.cooldown = TimeHelper.secToTick(COOLDOWN / amplifier);
        }

        if (this.doesEffects && --this.resetCooldown <= 0) {
            this.doesEffects = false;
            this.resetCooldown = TimeHelper.secToTick(this.random.nextLong(MIN_RESET_COOLDOWN,MAX_RESET_COOLDOWN));
        }
    }
}
