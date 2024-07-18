package com.zen.fogman.common.other;

import net.minecraft.client.render.Camera;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.*;

import java.lang.Math;

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

    public static boolean areBlocksAround(ServerWorld serverWorld, BlockPos pos, int rangeX, int rangeY, int rangeZ) {
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos,rangeX,rangeY,rangeZ)) {
            if (!serverWorld.getBlockState(blockPos).isAir()) {
                return true;
            }
        }
        return false;
    }
}
