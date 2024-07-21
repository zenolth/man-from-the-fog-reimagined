package com.zen.fogman.common.server;

import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManUtils;
import com.zen.fogman.common.gamerules.ModGamerules;
import com.zen.fogman.common.other.Util;
import com.zen.fogman.common.sounds.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class ModWorldEvents implements ServerEntityEvents.Load, ServerWorldEvents.Load, ServerTickEvents.EndWorldTick {

    public static final float MAN_CREEPY_VOLUME = 5f;

    public long spawnCooldown = Util.secToTick(15.0);

    public Random random = new Random();

    @Nullable
    public static ServerPlayerEntity getRandomAlivePlayer(ServerWorld serverWorld,Random random) {
        List<ServerPlayerEntity> list = serverWorld.getPlayers(entity -> entity.isAlive() && entity.getWorld().getRegistryKey() == World.OVERWORLD && !entity.isSpectator() && !entity.isCreative());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Generates a random position around position
     * @param serverWorld The World
     * @param origin Position to generate around
     * @param direction Direction the "player" is looking into
     * @param minRange Minimum range to generate
     * @param maxRange Maximum range to generate
     * @return The generated position
     */
    public static Vec3d getRandomSpawnBehindDirection(ServerWorld serverWorld, Random random, Vec3d origin, Vec3d direction, int minRange, int maxRange) {
        direction = direction.multiply(-1);
        direction = direction.multiply(random.nextInt(minRange,maxRange));
        direction = direction.rotateY((float) Math.toRadians((random.nextFloat(-60,60))));

        return serverWorld.getTopPosition(Heightmap.Type.WORLD_SURFACE,BlockPos.ofFloored(origin.add(direction))).toCenterPos();
    }

    /**
     * Generates a random position around position
     * @param serverWorld The World
     * @param origin Position to generate around
     * @param direction Direction the "player" is looking into
     * @return The generated position
     */
    public static Vec3d getRandomSpawnBehindDirection(ServerWorld serverWorld, Random random, Vec3d origin, Vec3d direction) {
        return getRandomSpawnBehindDirection(
                serverWorld,
                random,
                origin,
                direction,
                serverWorld.getGameRules().getInt(ModGamerules.MAN_MIN_SPAWN_RANGE),
                serverWorld.getGameRules().getInt(ModGamerules.MAN_MAX_SPAWN_RANGE)
        );
    }

    public static void playCreepySound(ServerWorld serverWorld,double x, double y, double z) {
        serverWorld.playSound(null,x,y,z, ModSounds.MAN_CREEPY, SoundCategory.AMBIENT,MAN_CREEPY_VOLUME,1f);
    }

    public static void spawnManAtRandomLocation(ServerWorld serverWorld,Random random) {
        ServerPlayerEntity player = getRandomAlivePlayer(serverWorld,random);
        if (player == null) {
            return;
        }
        Vec3d spawnPosition = getRandomSpawnBehindDirection(serverWorld,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        TheManEntity man = new TheManEntity(ModEntities.THE_MAN,serverWorld);
        if (man.canSpawn(serverWorld)) {
            man.setPosition(spawnPosition);
            man.setTarget(player);
            serverWorld.spawnEntity(man);
            if (!man.isSilent()) {
                playCreepySound(serverWorld,spawnPosition.getX(),spawnPosition.getY(),spawnPosition.getZ());
            }
        } else {
            man.discard();
        }
    }

    public static void playCreepySoundAtRandomLocation(ServerWorld serverWorld,Random random) {
        ServerPlayerEntity player = getRandomAlivePlayer(serverWorld,random);
        if (player == null) {
            return;
        }
        Vec3d soundPosition = getRandomSpawnBehindDirection(serverWorld,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        playCreepySound(serverWorld,soundPosition.getX(),soundPosition.getY(),soundPosition.getZ());
    }

    @Override
    public void onLoad(Entity entity, ServerWorld serverWorld) {
        if (entity instanceof TheManEntity theMan) {
            theMan.onSpawn(serverWorld);
        }
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld serverWorld) {
        if (serverWorld.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        spawnCooldown = Util.secToTick(serverWorld.getGameRules().get(ModGamerules.MAN_SPAWN_INTERVAL).get());
    }

    @Override
    public void onEndTick(ServerWorld serverWorld) {
        if (serverWorld.isClient()) {
            return;
        }
        if (serverWorld.isDay()) {
            return;
        }
        if (TheManUtils.manExists(serverWorld) || TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        GameRules gameRules = serverWorld.getGameRules();

        if (--spawnCooldown <= 0L) {

            int spawnChanceMul = gameRules.getBoolean(ModGamerules.MAN_SPAWN_CHANCE_SCALES) ? serverWorld.getPlayers().size() : 1;

            if (Math.random() < gameRules.get(ModGamerules.MAN_SPAWN_CHANCE).get() * spawnChanceMul) {
                if (Math.random() < gameRules.get(ModGamerules.MAN_AMBIENT_SOUND_CHANCE).get()) {
                    playCreepySoundAtRandomLocation(serverWorld,this.random);
                } else {
                    spawnManAtRandomLocation(serverWorld,this.random);
                }
            }

            spawnCooldown = Util.secToTick(gameRules.get(ModGamerules.MAN_SPAWN_INTERVAL).get());
        }
    }
}
