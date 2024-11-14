package com.zen.the_fog.common.server;

import com.zen.the_fog.common.entity.the_man.TheManPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModNetworking {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.LOOKED_AT_PACKET_ID,(server, player, handler, buf, responseSender) -> {
            boolean lookedAt = buf.readBoolean();
            player.the_fog_is_coming$setLookingAtMan(lookedAt);
        });
    }
}
