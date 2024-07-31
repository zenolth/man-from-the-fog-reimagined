package com.zen.fogman.common.server;

import com.zen.fogman.common.entity.the_man.TheManEntity;
import com.zen.fogman.common.entity.the_man.TheManPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;

public class ModNetworking {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.LOOKED_AT_PACKET_ID,(server, player, handler, buf, responseSender) -> {
            int mobId = buf.readInt();
            String uuid = buf.readString();
            boolean lookedAt = buf.readBoolean();
            Entity entity = player.getServerWorld().getEntityById(mobId);

            server.execute(() -> {
                if (entity == null) {
                    return;
                }

                if (entity instanceof TheManEntity theMan) {
                    theMan.updatePlayerMap(uuid,lookedAt);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.REMOVE_PLAYER_FROM_MAP_PACKET_ID,(server, player, handler, buf, responseSender) -> {
            int mobId = buf.readInt();
            String uuid = buf.readString();

            server.execute(() -> {
                Entity entity = server.getOverworld().getEntityById(mobId);

                if (entity == null) {
                    return;
                }

                if (entity instanceof TheManEntity theMan) {
                    theMan.playersLookingMap.remove(uuid);
                }
            });
        });
    }
}
