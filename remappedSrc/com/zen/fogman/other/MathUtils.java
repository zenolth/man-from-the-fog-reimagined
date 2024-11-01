package com.zen.the_fog.common.other;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtils {
    public static double distanceTo(Vec3d a,Vec3d b) {
        return a.subtract(b).length();
    }
    public static double distanceTo(Entity a, Entity b) {
        return a.getPos().subtract(b.getPos()).length();
    }
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

    public static double angleBetween(double x1,double y1,double x2,double y2) {
        return Math.atan2(y1 - y2,x1 - x2);
    }

    public static int toGoalTicks(int serverTicks) {
        return MathHelper.ceilDiv(serverTicks, 2);
    }
}
