package com.zen.the_fog.client.networking;

import com.zen.the_fog.client.mixin_interfaces.ClientPlayerEntityInterface;
import com.zen.the_fog.common.entity.the_man.TheManPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworking {
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(TheManPackets.UPDATE_FOG_DENSITY,(client,handler,buf,responseSender) -> {
            double fogDensity = buf.readDouble();

            System.out.println(fogDensity);

            if (client.player == null) return;

            System.out.println(client.player.getEntityName());

            client.player.the_fog_is_coming$setFogDensity(fogDensity);
        });
    }
}
