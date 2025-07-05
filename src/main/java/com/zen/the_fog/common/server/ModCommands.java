package com.zen.the_fog.common.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zen.the_fog.common.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap; // Import HashMap

import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    private static final String STATUS_JOINED = "JOINED";
    private static final String STATUS_DECLINED = "DECLINED";

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("manfromthefog")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("reloadConfig").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.of("Reloading config for Man From The Fog"),true);
                    Config.HANDLER.load();
                    context.getSource().sendFeedback(() -> Text.of("Reloaded"),true);
                    return 1;
                }))
            );

            registerTerrorCommands(dispatcher);
        });
    }

    private static void registerTerrorCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("terror")
            .then(literal("whitelist")
                .then(literal("join").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String playerUuid = player.getUuidAsString();
                    Config config = Config.get();

                    if (config.terrorPlayerList == null) {
                        config.terrorPlayerList = new HashMap<>();
                    }

                    config.terrorPlayerList.put(playerUuid, STATUS_JOINED);
                    Config.HANDLER.save();
                    context.getSource().sendFeedback(() -> Text.literal("Has decidido PARTICIPAR en las mecánicas de terror."), false);
                    return 1;
                }))
                .then(literal("decline").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String playerUuid = player.getUuidAsString();
                    Config config = Config.get();

                    if (config.terrorPlayerList == null) {
                        config.terrorPlayerList = new HashMap<>();
                    }

                    config.terrorPlayerList.put(playerUuid, STATUS_DECLINED);
                    Config.HANDLER.save();
                    context.getSource().sendFeedback(() -> Text.literal("Has decidido NO PARTICIPAR en las mecánicas de terror."), false);
                    return 1;
                }))
                // Optional: Command to reset status to undecided
                .then(literal("undecided").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String playerUuid = player.getUuidAsString();
                    Config config = Config.get();

                    if (config.terrorPlayerList != null && config.terrorPlayerList.containsKey(playerUuid)) {
                        config.terrorPlayerList.remove(playerUuid);
                        Config.HANDLER.save();
                        context.getSource().sendFeedback(() -> Text.literal("Tu preferencia sobre las mecánicas de terror ha sido reiniciada. Verás el mensaje de nuevo al unirte."), false);
                    } else {
                        context.getSource().sendFeedback(() -> Text.literal("No habías establecido una preferencia previamente."), false);
                    }
                    return 1;
                }))
            )
        );
    }
}
