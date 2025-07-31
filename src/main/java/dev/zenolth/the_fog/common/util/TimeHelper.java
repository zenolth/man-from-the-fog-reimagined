package dev.zenolth.the_fog.common.util;

public class TimeHelper {
    public static final long DAY_TICKS = 24000;

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

    public static long ticksToDays(long ticks) {
        return Math.round((double) ticks / DAY_TICKS);
    }
}
