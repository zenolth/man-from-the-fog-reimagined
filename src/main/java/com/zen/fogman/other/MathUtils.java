package com.zen.fogman.other;

import net.minecraft.entity.Entity;
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
    public static long secToTick(long secs) {
        return secs * 20;
    }
}
