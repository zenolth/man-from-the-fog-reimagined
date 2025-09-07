package com.zen.the_fog.common.server;

import com.zen.the_fog.common.components.ModComponents;
import com.zen.the_fog.common.components.TheManHealthComponent;
import com.zen.the_fog.common.config.Config;
import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManUtils;
import com.zen.the_fog.common.other.Util;
import com.zen.the_fog.common.sounds.ModSounds;
import com.zen.the_fog.common.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class ModWorldEvents implements ServerEntityEvents.Load, ServerWorldEvents.Load, ServerTickEvents.EndWorldTick, ServerPlayConnectionEvents.Join {

    public static final float MAN_CREEPY_VOLUME = 5f;

    public static final Predicate<? super ServerPlayerEntity> VALID_PLAYER_PREDICATE = player -> player.isAlive() && !player.isSpectator() && !player.isCreative() && TheManEntity.canAttack(player,player.getWorld());

    public long ticksBetweenSpawnAttempts = Util.secToTick(15.0);

    public Random random = new Random();

    @Nullable
    public static ServerPlayerEntity getRandomAlivePlayer(ServerWorld serverWorld,Random random) {
        List<ServerPlayerEntity> list = serverWorld.getPlayers(VALID_PLAYER_PREDICATE);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
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
        Vec3d spawnPosition = Util.getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        TheManEntity man = new TheManEntity(ModEntities.THE_MAN,world);
        if (man.canSpawn(world)) {
            man.setPosition(spawnPosition);
            man.setTarget(player);
            world.spawnEntity(man);
            if (!man.isSilent()) {
                playCreepySound(world,spawnPosition.getX(),spawnPosition.getY(),spawnPosition.getZ());
            }

            TheManHealthComponent healthComponent = ModComponents.THE_MAN_HEALTH.get(serverWorld);

            float newHealth = healthComponent.getValue();

            if (newHealth <= 0) {
                healthComponent.setValue((float) TheManEntity.createManAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
                newHealth = healthComponent.getValue();
            }

            man.setHealth(newHealth);
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
        Vec3d soundPosition = Util.getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        playCreepySound(world,soundPosition.getX(),soundPosition.getY(),soundPosition.getZ());
    }

    @Override
    public void onLoad(Entity entity, ServerWorld serverWorld) {
        if (entity instanceof TheManEntity theMan) {
            theMan.onSpawn(serverWorld);
        }
        /*if (entity instanceof ServerPlayerEntity player) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(serverWorld.getGameRules().get(ModGamerules.MAN_FOG_DENSITY_MOD).get());
            ServerPlayNetworking.send(player, TheManPackets.UPDATE_FOG_DENSITY, buf);
        }*/
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld serverWorld) {
        Config.load();

        ticksBetweenSpawnAttempts = Util.secToTick(Config.get().timeBetweenSpawnAttempts);
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {

    }

    @Override
    public void onEndTick(ServerWorld serverWorld) {
        if (serverWorld.getPlayers(VALID_PLAYER_PREDICATE).isEmpty()) {
            return;
        }

        if (!TheManEntity.isInAllowedDimension(serverWorld)) {
            return;
        }

        if (Util.isDay(serverWorld) && !Config.get().spawnInDay) {
            return;
        }

        if (TheManUtils.manExists(serverWorld) || TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        if (--ticksBetweenSpawnAttempts <= 0L) {

            int spawnChanceMul = Config.get().spawnChanceScalesWithPlayerCount ? serverWorld.getPlayers(VALID_PLAYER_PREDICATE).size() : 1;

            if (serverWorld.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
                spawnChanceMul *= 2;
            }

            double spawnChance = Config.get().spawnChance * spawnChanceMul;

            if (Config.get().spawnInDay && serverWorld.getRegistryKey() == World.OVERWORLD && Util.isDay(serverWorld)) {
                spawnChance /= 2.0;
            }

            if (Util.isBloodMoon(serverWorld)) {
                spawnChance *= 2;
            }

            if (Util.isSuperBloodMoon(serverWorld)) {
                spawnChance *= 10;
            }

            double ambientChance = Config.get().fakeSpawnChance;

            double spawnRandom = Math.random();
            double ambientRandom = Math.random();

            if (spawnRandom < spawnChance) {
                if (ambientRandom < ambientChance) {
                    playCreepySoundAtRandomLocation(serverWorld,this.random);
                } else {
                    spawnManAtRandomLocation(serverWorld,this.random);
                }
            }

            ticksBetweenSpawnAttempts = Util.secToTick(Config.get().timeBetweenSpawnAttempts);
        }
    }
}
