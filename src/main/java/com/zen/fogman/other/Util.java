package com.zen.fogman.other;

import net.minecraft.client.render.Camera;
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

    public static double angleBetween(double x1,double y1,double x2,double y2) {
        return Math.atan2(y1 - y2,x1 - x2);
    }
    public static double angleBetween(Vec3d a,Vec3d b) {
        a = a.normalize();
        b = b.normalize();
        double dot = a.dotProduct(b);
        return Math.acos(dot);
    }

    public static int toGoalTicks(int serverTicks) {
        return MathHelper.ceilDiv(serverTicks, 2);
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

    public static Vec2f worldToScreen(Matrix4f projection, Vector3f cameraPosition, Vector3f worldPosition) {
        Vector4f cameraCoords = new Vector4f(worldPosition.x,worldPosition.y,worldPosition.z,0f).sub(new Vector4f(cameraPosition.x,cameraPosition.y,cameraPosition.z,0f));
        Vector4f projectedCoords = projection.transform(cameraCoords);

        float xNDC = projectedCoords.x / projectedCoords.w;
        float yNDC = projectedCoords.y / projectedCoords.w;

        float xScreen = (xNDC + 1f) * 0.5f;
        float yScreen = (yNDC + 1f) * 0.5f;

        return new Vec2f(xScreen,yScreen);
    }

    public static boolean isInView(Matrix4f projection,Vec3d cameraPosition,Vec3d cameraForward,Vec3d point) {

        Vec3d toPoint = point.subtract(cameraPosition).normalize();
        double dot = cameraForward.dotProduct(toPoint);

        // Point is behind the camera, don't calculate other stuff because it's USELESS
        if (dot < 0) {
            return false;
        }

        Vec2f screenPos = worldToScreen(projection,cameraPosition.toVector3f(),point.toVector3f());

        return screenPos.x > 0f && screenPos.x < 1.5f;
    }
}
