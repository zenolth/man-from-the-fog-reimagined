package dev.zenolth.the_fog.common.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.function.Function;

public class Tween {
    private Vec3d start = Vec3d.ZERO;
    private Vec3d goal = Vec3d.ZERO;

    private boolean paused = true;
    private float alpha = 0f;
    private float speed = 1f;
    private final Function<Double,Double> easingFunction;

    public Tween(Function<Double,Double> easingFunction) {
        this.easingFunction = easingFunction;
    }

    public void start(Vec3d start,Vec3d goal,float speed) {
        if (!this.paused) return;
        this.alpha = 0;
        this.start = start;
        this.goal = goal;
        this.speed = speed;
        this.paused = false;
    }

    public void pause() {
        if (this.paused) return;
        this.paused = true;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public Vec3d getState() {
        return this.start.lerp(this.goal, MathHelper.clamp(this.easingFunction.apply((double) this.alpha),0f,1f));
    }

    public Vec3d getNormalizedState() {
        return GeometryHelper.slerp(this.start,this.goal, (float) MathHelper.clamp(this.easingFunction.apply((double) this.alpha),0f,1f));
    }

    public void tick() {
        if (this.alpha < 1f) {
            this.alpha += 0.1f * this.speed;
        } else {
            this.paused = true;
        }
    }
}
