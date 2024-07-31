package com.zen.fogman.common.server;

import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManPredicates;
import com.zen.fogman.common.entity.the_man.TheManUtils;
import com.zen.fogman.common.gamerules.ModGamerules;
import com.zen.fogman.common.other.Util;
import com.zen.fogman.common.sounds.ModSounds;
import com.zen.fogman.common.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.predicate.entity.PlayerPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
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
import java.util.function.Predicate;

public class ModWorldEvents implements ServerEntityEvents.Load, ServerWorldEvents.Load, ServerTickEvents.EndWorldTick, ServerPlayConnectionEvents.Disconnect {

    public static final float MAN_CREEPY_VOLUME = 5f;

    public static final Predicate<? super ServerPlayerEntity> VALID_PLAYER_PREDICATE = player -> player.isAlive() && !player.isSpectator() && !player.isCreative();

    public long spawnCooldown = Util.secToTick(15.0);

    public Random random = new Random();

    @Nullable
    public static ServerPlayerEntity getRandomAlivePlayer(ServerWorld serverWorld,Random random) {
        List<ServerPlayerEntity> list = serverWorld.getPlayers(VALID_PLAYER_PREDICATE);
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
        if (minRange == maxRange) {
            direction = direction.multiply(minRange);
        } else {
            direction = direction.multiply(maxRange > minRange ? random.nextInt(minRange,maxRange) : random.nextInt(maxRange,minRange));
        }
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
        ServerWorld world = player.getServerWorld();
        Vec3d spawnPosition = getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        TheManEntity man = new TheManEntity(ModEntities.THE_MAN,world);
        if (man.canSpawn(world)) {
            man.setPosition(spawnPosition);
            man.setTarget(player);
            world.spawnEntity(man);
            if (!man.isSilent()) {
                playCreepySound(world,spawnPosition.getX(),spawnPosition.getY(),spawnPosition.getZ());
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
        ServerWorld world = player.getServerWorld();
        Vec3d soundPosition = getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        playCreepySound(world,soundPosition.getX(),soundPosition.getY(),soundPosition.getZ());
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

        spawnCooldown = Util.secToTick(serverWorld.getGameRules().get(ModGamerules.MAN_SPAWN_COOLDOWN).get());
    }

    @Override
    public void onEndTick(ServerWorld serverWorld) {
        if (serverWorld.getPlayers(VALID_PLAYER_PREDICATE).isEmpty()) {
            return;
        }

        if (!TheManEntity.isInAllowedDimension(serverWorld)) {
            return;
        }

        if (serverWorld.getAmbientDarkness() < 4) {
            return;
        }

        if (TheManUtils.manExists(serverWorld) || TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        GameRules gameRules = serverWorld.getGameRules();

        if (--spawnCooldown <= 0L) {

            int spawnChanceMul = gameRules.getBoolean(ModGamerules.MAN_SPAWN_CHANCE_SCALES) ? serverWorld.getPlayers(VALID_PLAYER_PREDICATE).size() : 1;

            if (serverWorld.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
                spawnChanceMul *= 2;
            }

            double spawnChance = gameRules.get(ModGamerules.MAN_SPAWN_CHANCE).get() * spawnChanceMul;
            double ambientChance = gameRules.get(ModGamerules.MAN_AMBIENT_SOUND_CHANCE).get();

            double spawnRandom = Math.random();
            double ambientRandom = Math.random();

            if (spawnRandom < spawnChance) {
                if (ambientRandom < ambientChance) {
                    playCreepySoundAtRandomLocation(serverWorld,this.random);
                } else {
                    spawnManAtRandomLocation(serverWorld,this.random);
                }
            }

            spawnCooldown = Util.secToTick(gameRules.get(ModGamerules.MAN_SPAWN_COOLDOWN).get());
        }
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        ServerWorld serverWorld = player.getServerWorld();

        List<? extends TheManEntity> theManEntities = serverWorld.getEntitiesByType(ModEntities.THE_MAN, TheManPredicates.VALID_MAN);

        theManEntities.forEach(theManEntity -> {
            theManEntity.removePlayerFromMap(player.getUuidAsString());
        });
    }
}
