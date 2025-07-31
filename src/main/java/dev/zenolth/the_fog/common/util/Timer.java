package dev.zenolth.the_fog.common.util;

import java.util.function.Supplier;

public class Timer {
    private final Supplier<Integer> tickSupplier;
    private long ticks;
    private final Runnable runnable;
    private boolean running = false;
    private boolean loop = false;

    public Timer(Supplier<Integer> tickSupplier, boolean loop, Runnable runnable) {
        this.tickSupplier = tickSupplier;
        this.ticks = this.tickSupplier.get();
        this.loop = loop;
        this.runnable = runnable;
    }

    public Timer(Supplier<Integer> tickSupplier, Runnable runnable) {
        this(tickSupplier, false, runnable);
    }

    public Timer(int ticks, boolean loop, Runnable runnable) {
        this(() -> ticks, loop, runnable);
    }

    public Timer(int ticks, Runnable runnable) {
        this(ticks,false,runnable);
    }

    public void start() {
        if (this.running) return;
        this.ticks = this.tickSupplier.get();
        this.running = true;
    }

    public void setTicks(long ticks) {
        this.ticks = ticks;
    }

    public long ticks() {
        return this.ticks;
    }

    public void pause() {
        this.running = false;
    }

    public void resume() {
        this.running = true;
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isLooping() {
        return this.loop;
    }

    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    public void tick() {
        if (this.running && --this.ticks <= 0L) {
            this.runnable.run();
            this.running = false;
        }
        if (this.loop) this.start();
    }
}
