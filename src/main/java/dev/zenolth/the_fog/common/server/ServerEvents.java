package dev.zenolth.the_fog.common.server;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.components.WorldComponent;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.entity.MonitorPlayerLineOfSight;
import dev.zenolth.the_fog.common.entity.OnSpawnEntity;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.util.*;
import dev.zenolth.the_fog.common.mixin_interfaces.GroupInterface;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.world.dimension.ModDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ServerEvents implements
        ServerEntityEvents.Load,
        ServerEntityEvents.Unload,

        ServerWorldEvents.Load,

        ServerTickEvents.EndTick,
        ServerTickEvents.EndWorldTick,

        ServerPlayConnectionEvents.Join,
        ServerPlayConnectionEvents.Disconnect {

    private static ServerEvents INSTANCE;

    public static final long CHECK_DAYS_TICKS = 40;
    public static final float MAN_CREEPY_VOLUME = 5f;

    public static final Predicate<? super PlayerEntity> VALID_PLAYER_PREDICATE = player -> player.isAlive() && !player.isSpectator() && !player.isCreative() && TheManEntity.canAttack(player,player.getWorld());

    public long checkDaysTicks = CHECK_DAYS_TICKS;

    private ServerEvents() {}

    public static ServerEvents getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerEvents();
        }
        return INSTANCE;
    }

    @Nullable
    public static ServerPlayerEntity getRandomAlivePlayer(ServerWorld world) {
        List<ServerPlayerEntity> list = world.getPlayers(VALID_PLAYER_PREDICATE);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        var playerCount = list.size() - 1;
        RandomCollection<ServerPlayerEntity> players = new RandomCollection<>();
        for (var player : list) {
            var weight = 1.0 - MathHelper.clamp((double) ((GroupInterface) player).the_fog_is_coming$getPlayersInGroupCount() / playerCount,0.2,0.8);
            players.add(weight,player);
        }
        return players.next();
    }

    public static void playCreepySound(ServerWorld world,double x, double y, double z) {
        world.playSound(null,x,y,z, ModSounds.MAN_CREEPY, SoundCategory.MASTER,MAN_CREEPY_VOLUME,1f);
    }

    public static void spawnEntityAtRandomLocation(ServerWorld world, EntityType<? extends HostileEntity> entityType) {
        var player = getRandomAlivePlayer(world);
        if (player == null) {
            return;
        }
        var spawnPosition = WorldHelper.getRandomSpawnBehindDirection(world,player.getPos(), GeometryHelper.calculateDirection(0,player.getYaw(1.0f)));
        var entity = entityType.create(world);
        if (entity == null) return;
        if (entity.canSpawn(world)) {
            entity.setPosition(spawnPosition);
            entity.setTarget(player);
            world.spawnEntity(entity);

            if (entity instanceof TheManEntity theMan) {
                if (!theMan.isSilent()) {
                    playCreepySound(world,spawnPosition.getX(),spawnPosition.getY(),spawnPosition.getZ());
                }

                var newHealth = WorldComponent.get(world).theManHealth();

                if (newHealth <= 0) {
                    WorldComponent.get(world).setTheManHealth((float) TheManEntity.createAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
                    newHealth = WorldComponent.get(world).theManHealth();
                }

                theMan.setHealth(newHealth);
            }
        } else {
            entity.discard();
        }
    }

    public static void playCreepySoundAtRandomLocation(ServerWorld world) {
        var player = getRandomAlivePlayer(world);
        if (player == null) {
            return;
        }
        var soundPosition = WorldHelper.getRandomSpawnBehindDirection(world,player.getPos(), GeometryHelper.calculateDirection(0,player.getYaw(1.0f)));
        playCreepySound(world,soundPosition.getX(),soundPosition.getY(),soundPosition.getZ());
    }

    @Override
    public void onLoad(Entity entity, ServerWorld world) {
        if (entity instanceof OnSpawnEntity m) {
            m.onSpawn(world);
        }
        /*if (entity instanceof ServerPlayerEntity player) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(world.getGameRules().get(ModGamerules.MAN_FOG_DENSITY_MOD).get());
            ServerPlayNetworking.send(player, PacketTypes.UPDATE_FOG_DENSITY, buf);
        }*/
    }

    @Override
    public void onUnload(Entity entity, ServerWorld world) {
        if (entity instanceof TheManEntity) {
            WorldComponent.get(world).setTheManId(null);
        }
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld world) {
        TheManEntity.resetKilledCount(world);
        WorldComponent.get(world).setSpawnAttemptTicks(TimeHelper.secToTick(FogMod.CONFIG.spawning.timeBetweenSpawnAttempts));

        var men = world.getEntitiesByType(TypeFilter.instanceOf(TheManEntity.class), TheManEntity::isReal);

        if (!men.isEmpty()) {
            for (var theMan : men) {
                if (theMan.isReal()) {
                    WorldComponent.get(world).setTheManId(theMan.getId());
                    break;
                }
            }
        } else {
            WorldComponent.get(world).setTheManId(null);
        }
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        WorldComponent.syncWith(handler.getPlayer());
        Console.writeln("%s joined.",handler.getPlayer().getGameProfile().getName());
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        var player = handler.getPlayer();
        var entities = player.getServerWorld().getEntitiesByType(TypeFilter.instanceOf(Entity.class),(e) -> e instanceof MonitorPlayerLineOfSight);
        entities.forEach(entity -> {
            ((MonitorPlayerLineOfSight) entity).setPlayerLOS(player.getGameProfile().getId(),false);
        });
    }

    @Override
    public void onEndTick(ServerWorld world) {
        if (WorldComponent.get(world).spawnAttemptTicks() > 0L) {
            WorldComponent.get(world).setSpawnAttemptTicks(WorldComponent.get(world).spawnAttemptTicks() - 1);
            return;
        }

        if (!TheManEntity.isInAllowedDimension(world)) {
            return;
        }

        if (WorldHelper.isDay(world) && !FogMod.CONFIG.spawning.spawnInDay) {
            return;
        }

        if (TheManUtils.manExists(world) || TheManUtils.hallucinationsExists(world)) {
            return;
        }

        if (TheManEntity.getKilledCount(world) >= FogMod.CONFIG.spawning.maxKillCount) {
            if (--this.checkDaysTicks <= 0L) {
                var currentDay = TimeHelper.ticksToDays(world.getTimeOfDay());

                if ((currentDay - TheManEntity.getDayKilled(world)) >= FogMod.CONFIG.spawning.dayAmountToStopSpawn) {
                    TheManEntity.resetKilledCount(world);
                }

                this.checkDaysTicks = CHECK_DAYS_TICKS;
            }
            return;
        }

        if (world.getPlayers(VALID_PLAYER_PREDICATE).isEmpty()) {
            return;
        }

        var spawnChanceMul = FogMod.CONFIG.spawning.spawnChanceScalesWithPlayerCount ? world.getPlayers(VALID_PLAYER_PREDICATE).size() : 1;

        if (world.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
            spawnChanceMul *= 3;
        }

        var spawnChance = FogMod.CONFIG.spawning.spawnChance * spawnChanceMul;

        if (FogMod.CONFIG.spawning.spawnInDay && world.getRegistryKey() == World.OVERWORLD && WorldHelper.isDay(world)) {
            spawnChance /= 2.0f;
        }

        if (WorldHelper.isBloodMoon(world)) {
            spawnChance *= 4;
        }

        if (WorldHelper.isSuperBloodMoon(world)) {
            spawnChance = 2;
        }

        var ambientChance = FogMod.CONFIG.spawning.fakeSpawnChance;

        var spawnRandom = RandomNum.nextFloat();
        var ambientRandom = RandomNum.nextFloat();

        if (spawnRandom < spawnChance) {
            if (ambientRandom < ambientChance) {
                playCreepySoundAtRandomLocation(world);
            } else {
                spawnEntityAtRandomLocation(world,ModEntities.THE_MAN);
            }
        }

        WorldComponent.get(world).setSpawnAttemptTicks(TimeHelper.secToTick(FogMod.CONFIG.spawning.timeBetweenSpawnAttempts));
    }

    @Override
    public void onEndTick(MinecraftServer server) {

    }
}
