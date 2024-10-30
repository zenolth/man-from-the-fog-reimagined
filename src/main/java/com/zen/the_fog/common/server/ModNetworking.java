package com.zen.the_fog.common.server;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManPackets;
import com.zen.the_fog.common.gamerules.ModGamerules;
import com.zen.the_fog.common.mixin_interfaces.LookingAtManInterface;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class ModNetworking {

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.LOOKED_AT_PACKET_ID,(server, player, handler, buf, responseSender) -> {
            boolean lookedAt = buf.readBoolean();
            ((LookingAtManInterface) player).the_fog_is_coming$setLookingAtMan(lookedAt);
        });

        ServerPlayNetworking.registerGlobalReceiver(TheManPackets.UPDATE_FOG_DENSITY,((server, player, handler, buf, responseSender) -> {
            PacketByteBuf newBuf = PacketByteBufs.create();
            newBuf.writeDouble(player.getServerWorld().getGameRules().get(ModGamerules.MAN_FOG_DENSITY_MOD).get());
            ServerPlayNetworking.send(player, TheManPackets.UPDATE_FOG_DENSITY, newBuf);
        }));
    }
}
