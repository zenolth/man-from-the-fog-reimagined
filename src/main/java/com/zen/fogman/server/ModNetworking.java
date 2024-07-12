package com.zen.fogman.server;

import com.zen.fogman.entity.the_man.TheManEntity;
import com.zen.fogman.entity.the_man.TheManPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;

public class ModNetworking {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.LOOKED_AT_PACKET_ID,(server, player, handler, buf, responseSender) -> {
            Entity entity = server.getOverworld().getEntityById(buf.readInt());

            if (entity == null) {
                return;
            }

            if (entity instanceof TheManEntity theMan) {
                theMan.setLookedAt(buf.readBoolean());
            }
        });
    }
}
