package dev.zenolth.the_fog.common.server;

import dev.zenolth.the_fog.common.entity.MonitorPlayerLineOfSight;
import dev.zenolth.the_fog.common.networking.PacketTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModNetworking {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(PacketTypes.LINE_OF_SIGHT,(server, player, handler, buf, responseSender) -> {
            var uuid = buf.readUuid();
            var los = buf.readBoolean();
            var entity = player.getServerWorld().getEntity(uuid);
            if (entity instanceof MonitorPlayerLineOfSight losEntity) {
                losEntity.setPlayerLOS(player.getGameProfile().getId(),los);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PacketTypes.REQUEST_SYNC_CONFIG,(server, player, networkHandler, buf, sender) -> {

        });
    }
}
