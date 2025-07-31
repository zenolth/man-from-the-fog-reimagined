package dev.zenolth.the_fog.common.entity;

import dev.zenolth.the_fog.common.networking.PacketTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

import java.util.HashSet;
import java.util.UUID;

public interface MonitorPlayerLineOfSight {

    HashSet<UUID> getPlayersWithLOS();

    @Environment(EnvType.CLIENT)
    default void setPlayerLOS(boolean los) {
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeUuid(((Entity) this).getUuid());
        packet.writeBoolean(los);
        ClientPlayNetworking.send(PacketTypes.LINE_OF_SIGHT,packet);
    }
    default void setPlayerLOS(UUID uuid,boolean los) {
        if (los) {
            this.getPlayersWithLOS().add(uuid);
        } else {
            this.getPlayersWithLOS().remove(uuid);
        }
    }
    default boolean getPlayerLOS(UUID uuid) {
        return this.getPlayersWithLOS().contains(uuid);
    }
    default boolean isLookedAt() {
        return !this.getPlayersWithLOS().isEmpty();
    }
}
