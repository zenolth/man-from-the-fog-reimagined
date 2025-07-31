package dev.zenolth.the_fog.common.util;

import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.stream.IntStream;

public class GeometryHelper {
    /**
     * Calculates a normalized direction {@link Vec3d} from a pitch and yaw.
     * @param pitch Angle in degrees
     * @param yaw Angle in degrees
     * @return Normalized direction {@link Vec3d}
     */
    public static Vec3d calculateDirection(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    /**
     * Calculates a flat distance (height not taken into account) between points a and b.
     * @param a Point a
     * @param b Point b
     * @return Distance between a and b disregarding their y.
     */
    public static double getFlatDistance(Vec3d a, Vec3d b) {
        double d = b.x - a.x;
        double e = b.z - a.z;

        return Math.sqrt(d * d + e * e);
    }

    /**
     * Calculates a flat distance (height not taken into account) between points a and b.
     * @param a Point a
     * @param b Point b
     * @return Distance between a and b disregarding their y.
     */
    public static int getFlatDistance(Vec3i a, Vec3i b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Linearly interpolates between 2 normalized {@link Vec3d}s
     * @param start Normalized {@link Vec3d}
     * @param end Normalized {@link Vec3d}
     * @param alpha Number between 0 and 1, "progress" of the interpolate
     * @return Normalized {@link Vec3d}
     */
    public static Vec3d slerp(Vec3d start,Vec3d end,float alpha) {
        var dot = start.dotProduct(end);
        dot = MathHelper.clamp(dot,-1.0,1.0);

        var theta = Math.acos(dot) * alpha;
        var relativeVec = end.subtract(start).multiply(dot).normalize();
        return start.multiply(Math.cos(theta)).add(relativeVec.multiply(Math.sin(theta)));
    }

    public static int getChebyshevDistance(Vec3i a, Vec3i b) {
        int xDistance = Math.abs(b.getX() - a.getX());
        int yDistance = Math.abs(b.getY() - a.getY());
        int zDistance = Math.abs(b.getZ() - a.getZ());
        return IntStream.of(xDistance,yDistance,zDistance).max().getAsInt();
    }

    /**
     * Calculates pitch and yaw between point a and point b
     * @param a
     * @param b
     * @return A pair where the left side is the Pitch and right side is the Yaw
     */
    public static Pair<Float,Float> getRotationBetween(Vec3d a, Vec3d b) {
        var dir = b.subtract(a).normalize();
        var pitch = (float) Math.asin(-dir.y);
        var yaw = (float) Math.atan2(dir.x,dir.z);
        return new Pair<>(pitch,-yaw);
    }

    public static float interpolate(float a, float b, float f)
    {
        return a * (1f - f) + (b * f);
    }
}
