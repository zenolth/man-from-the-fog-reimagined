package com.zen.the_fog.common.server;

import com.zen.the_fog.common.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("manfromthefog")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("reloadConfig").executes(context -> {
                    context.getSource().sendFeedback(() -> Text.of("Reloading config for Man From The Fog"),true);

                    Config.load();

                    context.getSource().sendFeedback(() -> Text.of("Reloaded"),true);

                    return 1;
                }))
            );
        });
    }
}
