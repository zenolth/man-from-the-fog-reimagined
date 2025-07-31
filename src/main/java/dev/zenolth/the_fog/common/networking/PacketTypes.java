package dev.zenolth.the_fog.common.networking;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.util.Identifier;

public class PacketTypes {
    public static final Identifier LINE_OF_SIGHT = FogMod.id("line_of_sight");
    public static final Identifier SYNC_CONFIG = FogMod.id("sync_config");
    public static final Identifier REQUEST_SYNC_CONFIG = FogMod.id("request_sync_config");
}
