package com.zen.fogman.common.other;

import com.zen.fogman.common.gamerules.ModGamerules;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.joml.*;

import java.lang.Math;
import java.util.Random;

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

    public static double get2dDistance(Vec3d a,Vec3d b) {
        a = new Vec3d(a.getX(),0,a.getZ());
        b = new Vec3d(b.getX(),0,b.getZ());
        return a.distanceTo(b);
    }

    public static boolean isDay(World world) {
        world.calculateAmbientDarkness();
        return world.getAmbientDarkness() < 4;
    }

    public static boolean isNight(World world) {
        return !isDay(world);
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

        return serverWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,BlockPos.ofFloored(origin.add(direction))).up().toCenterPos();
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

    public static boolean areBlocksAround(ServerWorld serverWorld, BlockPos pos, int rangeX, int rangeY, int rangeZ) {
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos,rangeX,rangeY,rangeZ)) {
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (!blockState.isAir() && blockState.isFullCube(serverWorld,blockPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areBlocksAround(ServerWorld serverWorld, BlockPos pos, int rangeY) {
        for (int y = 1 ; y <= rangeY ; y++) {
            BlockPos blockPos = pos.up(y);
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (!blockState.isAir() && blockState.isFullCube(serverWorld,blockPos)) {
                return true;
            }
        }
        return false;
    }
}
