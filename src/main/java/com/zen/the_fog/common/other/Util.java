package com.zen.the_fog.common.other;

import com.zen.the_fog.common.config.Config;
import com.zen.the_fog.common.entity.the_man.TheManEntity;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.lang.Math;
import java.util.Random;
import java.util.Optional;

public class Util {
    public static long tickToSec(long ticks) {
        return ticks / 20;
    }
    public static int secToTick(long secs) {
        return (int) (secs * 20);
    }

    public static double tickToSec(double ticks) {
        return ticks / 20;
    }
    public static int secToTick(double secs) {
        return (int) (secs * 20);
    }

    public static Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public static double getFlatDistance(Vec3d a, Vec3d b) {
        double d = b.x - a.x;
        double e = b.z - a.z;

        return Math.sqrt(d * d + e * e);
    }

    public static float lerp(float a, float b, float f)
    {
        return a * (1f - f) + (b * f);
    }

    public static boolean isDay(World world) {
        world.calculateAmbientDarkness();
        return world.getAmbientDarkness() < 4;
    }

    public static boolean isNight(World world) {
        return !isDay(world);
    }

    public static boolean isEnhancedCelestialsPresent() {
        return FabricLoader.getInstance().isModLoaded("enhancedcelestials");
    }

    public static boolean isBloodMoon(World world) {
        if (!isEnhancedCelestialsPresent()) return false;

        Optional<EnhancedCelestialsLunarForecastWorldData> optData = EnhancedCelestials.lunarForecastWorldData(world);

        if (optData.isEmpty()) return false;

        EnhancedCelestialsLunarForecastWorldData data = optData.get();

        return data.currentLunarEventHolder().getKey().isPresent()
                && data.currentLunarEventHolder().getKey().get() == DefaultLunarEvents.BLOOD_MOON;
    }

    public static boolean isSuperBloodMoon(World world) {
        if (!isEnhancedCelestialsPresent()) return false;

        Optional<EnhancedCelestialsLunarForecastWorldData> optData = EnhancedCelestials.lunarForecastWorldData(world);

        if (optData.isEmpty()) return false;

        EnhancedCelestialsLunarForecastWorldData data = optData.get();

        return data.currentLunarEventHolder().getKey().isPresent()
                && data.currentLunarEventHolder().getKey().get() == DefaultLunarEvents.SUPER_BLOOD_MOON;
    }

    /**
     * Generates a random position around position
     * @param world The World
     * @param origin Position to generate around
     * @param direction Direction the "player" is looking into
     * @param minRange Minimum range to generate
     * @param maxRange Maximum range to generate
     * @return The generated position
     */
    public static Vec3d getRandomSpawnBehindDirection(WorldView world, Random random, Vec3d origin, Vec3d direction, int minRange, int maxRange) {
        direction = direction.multiply(-1);
        direction = direction.rotateY((float) Math.toRadians((random.nextFloat(-60,60))));

        Vec3d normalizedDirection = direction.normalize();
        int range;

        if (minRange == maxRange) {
            range = minRange;
        } else {
            range = maxRange > minRange ? random.nextInt(minRange,maxRange) : random.nextInt(maxRange,minRange);
        }

        int initialRange = range;

        Vec3d spawnDirection = normalizedDirection.multiply(initialRange);

        BlockPos blockPos = getTopPosition(world,BlockPos.ofFloored(origin.add(spawnDirection)));

        while (TheManEntity.getRepellentAroundPosition(blockPos,world,15) != null) {
            initialRange += 15;
            spawnDirection = normalizedDirection.multiply(initialRange);
            blockPos = getTopPosition(world,BlockPos.ofFloored(origin.add(spawnDirection)));
        }

        return blockPos.toCenterPos();
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
                Config.get().minSpawnRange,
                Config.get().maxSpawnRange
        );
    }

    public static BlockPos getTopPosition(WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        while (!blockState.isAir()) {
            pos = pos.up();
            blockState = world.getBlockState(pos);
            if (pos.getY() >= world.getTopY()) {
                break;
            }
        }

        BlockState blockStateDown = world.getBlockState(pos.down());

        while (blockStateDown.isAir()) {
            pos = pos.down();
            blockStateDown = world.getBlockState(pos.down());
            if (pos.getY() < world.getBottomY()) {
                break;
            }
        }

        return pos;
    }

    public static boolean isBlockPresent(WorldView worldView, BlockPos pos) {
        BlockState blockState = worldView.getBlockState(pos);

        return !blockState.isAir() && blockState.isFullCube(worldView, pos);
    }

    public static boolean areBlocksAround(WorldView worldView, BlockPos pos, int rangeX, int rangeY, int rangeZ) {
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos,rangeX,rangeY,rangeZ)) {
            BlockState blockState = worldView.getBlockState(blockPos);
            if (!blockState.isAir() && blockState.isFullCube(worldView,blockPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areBlocksAround(WorldView worldView, BlockPos pos, int rangeY) {
        for (int y = 1 ; y <= rangeY ; y++) {
            BlockPos blockPos = pos.up(y);
            BlockState blockState = worldView.getBlockState(blockPos);
            if (!blockState.isAir() && blockState.isFullCube(worldView,blockPos)) {
                return true;
            }
        }
        return false;
    }
}