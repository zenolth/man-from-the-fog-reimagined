package dev.zenolth.the_fog.common.server.chat;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.util.TimeHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class ChatLog implements ServerMessageEvents.ChatMessage, ServerTickEvents.EndTick {
    private static ChatLog INSTANCE;

    public static final int MESSAGE_LIFETIME_CHECK_COOLDOWN = 18000;

    public record MessageLog(UUID senderUUID, Text content, int lifeTime) {}

    private final HashMap<UUID, HashSet<MessageLog>> logs = new HashMap<>();
    private int messageLifeTimeCheck = MESSAGE_LIFETIME_CHECK_COOLDOWN;

    private ChatLog() {}

    public static ChatLog getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatLog();
        return INSTANCE;
    }

    public Optional<HashSet<MessageLog>> getMessageLogs(UUID playerUUID) {
        if (!this.logs.containsKey(playerUUID)) return Optional.empty();
        return Optional.of(this.logs.get(playerUUID));
    }

    @Override
    public void onChatMessage(SignedMessage message, ServerPlayerEntity player, MessageType.Parameters parameters) {
        var id = player.getGameProfile().getId();
        if (!this.logs.containsKey(player.getUuid())) {
            this.logs.put(id,new HashSet<>());
        }

        this.logs.get(id).add(new MessageLog(
                id,
                message.getContent(),
                player.getServer().getTicks() + TimeHelper.secToTick(FogMod.CONFIG.miscellaneous.chatLogLifetime * 60L))
        );
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (--this.messageLifeTimeCheck <= 0L) {
            for (var messages : this.logs.values()) {
                messages.removeIf(message -> message.lifeTime > server.getTicks());
            }
            this.messageLifeTimeCheck = MESSAGE_LIFETIME_CHECK_COOLDOWN;
        }
    }
}
