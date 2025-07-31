package dev.zenolth.the_fog.common.util;

import java.util.Random;

public class RandomNum {
    private static final Random random = new Random();

    /**
     * Returns a random {@link Double} between 0.0 and 1.0
     * @return {@link Double} in range 0.0-1.0
     */
    public static double nextDouble() {
        return next(0.0,1.0);
    }

    /**
     * Returns a random {@link Float} between 0.0f and 1.0f
     * @return {@link Float} in range 0.0f-1.0f
     */
    public static float nextFloat() {
        return next(0.0f,1.0f);
    }

    public static boolean nextBoolean() {
        return random.nextBoolean();
    }

    public static int next(int max) {
        return next(0,max);
    }

    public static int next(int min,int max) {
        if (min == max) return min;
        return random.nextInt(min,max);
    }

    public static long next(long max) {
        return next(0L,max);
    }

    public static long next(long min,long max) {
        if (min == max) return min;
        return random.nextLong(min,max);
    }

    public static float next(float max) {
        return next(0f,max);
    }

    public static float next(float min,float max) {
        if (min == max) return min;
        return random.nextFloat(min,max);
    }

    public static double next(double max) {
        return next(0.0,max);
    }

    public static double next(double min,double max) {
        if (min == max) return min;
        return random.nextDouble(min,max);
    }

    public static void setSeed(long seed) {
        random.setSeed(seed);
    }

    public static Random get() {
        return random;
    }
}
