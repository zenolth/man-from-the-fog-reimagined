package dev.zenolth.the_fog.common.util;

import dev.zenolth.the_fog.common.FogMod;

public class Console {
    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    public static void writeln(Object message) {
        writeln(message, Severity.INFO);
    }

    public static void writeln(Object message, Severity severity) {
        switch (severity) {
            case INFO: FogMod.LOGGER.info(message.toString()); break;
            case WARNING: FogMod.LOGGER.warn(message.toString()); break;
            case ERROR: FogMod.LOGGER.error(message.toString()); break;
        }
    }

    public static void writeln(String message, Object... objects) {
        writeln(String.format(message,objects));
    }

    public static void writeln(String message, Severity severity, Object... objects) {
        writeln(String.format(message,objects),severity);
    }
}
