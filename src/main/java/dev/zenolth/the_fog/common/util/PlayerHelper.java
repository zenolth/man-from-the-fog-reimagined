package dev.zenolth.the_fog.common.util;

import dev.zenolth.the_fog.common.block.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerHelper {
    public static boolean isHidden(PlayerEntity player) {
        var world = player.getWorld();
        return !player.isDead() && player.isSneaking() && world.getBlockState(player.getBlockPos()).isIn(ModTags.COVER);
    }

    @Nullable
    public static ServerPlayerEntity getPlayerById(MinecraftServer server, UUID id) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (player.getGameProfile().getId().equals(id)) {
                return player;
            }
        }

        return null;
    }

    public static boolean isPlayerWithIdPresent(MinecraftServer server, UUID id, boolean offlinePlayers) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            if (player.getGameProfile().getId().equals(id)) {
                return true;
            }
        }

        if (offlinePlayers) {
            var cache = server.getUserCache();
            if (cache != null) {
                for (var entry : cache.load()) {
                    var profile = entry.getProfile();
                    if (profile.getId().equals(id)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isPlayerWithIdPresent(MinecraftServer server, UUID id) {
        return isPlayerWithIdPresent(server,id,false);
    }
}
