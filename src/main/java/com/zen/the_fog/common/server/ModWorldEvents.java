package com.zen.the_fog.common.server;

import com.zen.the_fog.common.ManFromTheFog;
import com.zen.the_fog.common.components.ModComponents;
import com.zen.the_fog.common.components.TheManHealthComponent;
import com.zen.the_fog.common.config.Config;
import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.entity.the_man.TheManEntity;
import com.zen.the_fog.common.entity.the_man.TheManUtils;
import com.zen.the_fog.common.item.ModItems;
import com.zen.the_fog.common.other.Util;
import com.zen.the_fog.common.sounds.ModSounds;
import com.zen.the_fog.common.world.dimension.ModDimensions;
import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents; // Keep for now, might be used elsewhere or good for reference
import corgitaco.enhancedcelestials.api.lunarevent.LunarEvent; // Added
import corgitaco.enhancedcelestials.core.EnhancedCelestialsContext;
import corgitaco.enhancedcelestials.lunarevent.LunarForecast;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.resources.ResourceKey; // Added
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional; // Added
import java.util.Random;
import java.util.function.Predicate;

public class ModWorldEvents implements ServerEntityEvents.Load, ServerWorldEvents.Load, ServerTickEvents.EndWorldTick, ServerPlayConnectionEvents.Join {

    public static final float MAN_CREEPY_VOLUME = 5f;
    private static final String STATUS_JOINED = "JOINED";
    // private static final String STATUS_DECLINED = "DECLINED"; // Not strictly needed here but good for consistency

    // Updated VALID_PLAYER_PREDICATE to check the terrorPlayerList map
    public static final Predicate<? super ServerPlayerEntity> VALID_PLAYER_PREDICATE = player -> {
        Config config = Config.get();
        if (config.terrorPlayerList == null) {
            return false; // Should not happen if config is loaded
        }
        String playerStatus = config.terrorPlayerList.getOrDefault(player.getUuidAsString(), ""); // Default to empty if not in map
        return player.isAlive() &&
               !player.isSpectator() &&
               !player.isCreative() &&
               TheManEntity.canAttack(player, player.getWorld()) &&
               STATUS_JOINED.equals(playerStatus);
    };

    public long ticksBetweenSpawnAttempts = Util.secToTick(15.0);

    public Random random = new Random();

    @Nullable
    public static ServerPlayerEntity getRandomAlivePlayer(ServerWorld serverWorld,Random random) {
        List<ServerPlayerEntity> list = serverWorld.getPlayers(VALID_PLAYER_PREDICATE);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    public static void playCreepySound(ServerWorld serverWorld,double x, double y, double z) {
        serverWorld.playSound(null,x,y,z, ModSounds.MAN_CREEPY, SoundCategory.AMBIENT,MAN_CREEPY_VOLUME,1f);
    }

    public static void spawnManAtRandomLocation(ServerWorld serverWorld,Random random) {
        ServerPlayerEntity player = getRandomAlivePlayer(serverWorld,random);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        Vec3d spawnPosition = Util.getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        TheManEntity man = new TheManEntity(ModEntities.THE_MAN,world);
        if (man.canSpawn(world)) {
            man.setPosition(spawnPosition);
            man.setTarget(player);
            world.spawnEntity(man);
            if (!man.isSilent()) {
                playCreepySound(world,spawnPosition.getX(),spawnPosition.getY(),spawnPosition.getZ());
            }

            TheManHealthComponent healthComponent = ModComponents.THE_MAN_HEALTH.get(serverWorld);

            float newHealth = healthComponent.getValue();

            if (newHealth <= 0) {
                healthComponent.setValue((float) TheManEntity.createManAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH));
                newHealth = healthComponent.getValue();
            }

            man.setHealth(newHealth);
        } else {
            man.discard();
        }
    }

    public static void playCreepySoundAtRandomLocation(ServerWorld serverWorld,Random random) {
        ServerPlayerEntity player = getRandomAlivePlayer(serverWorld,random);
        if (player == null) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        Vec3d soundPosition = Util.getRandomSpawnBehindDirection(world,random,player.getPos(), Util.getRotationVector(0,player.getYaw(1.0f)));
        playCreepySound(world,soundPosition.getX(),soundPosition.getY(),soundPosition.getZ());
    }

    @Override
    public void onLoad(Entity entity, ServerWorld serverWorld) {
        if (entity instanceof TheManEntity theMan) {
            theMan.onSpawn(serverWorld);
        }
        /*if (entity instanceof ServerPlayerEntity player) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(serverWorld.getGameRules().get(ModGamerules.MAN_FOG_DENSITY_MOD).get());
            ServerPlayNetworking.send(player, TheManPackets.UPDATE_FOG_DENSITY, buf);
        }*/
    }

    @Override
    public void onWorldLoad(MinecraftServer server, ServerWorld serverWorld) {
        Config.load();

        ticksBetweenSpawnAttempts = Util.secToTick(Config.get().timeBetweenSpawnAttempts);
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;
        String playerUuid = player.getUuidAsString();
        Config config = Config.get();

        if (config.terrorPlayerList == null) {
            // This should ideally not happen if YACL initializes config correctly.
            // If it's null, creating a new one here might not be saved correctly by YACL unless explicitly handled.
            // For now, log a warning and assume it means undecided.
            ManFromTheFog.LOGGER.warn("Config.terrorPlayerList is null during onPlayReady for player " + playerUuid + ". Assuming undecided.");
            // To be safe, skip sending message if map is null, as getOrDefault would fail.
            // However, the predicate also checks for null, so this state should be handled.
        }

        // Player is "UNDECIDED" if not in the map.
        if (config.terrorPlayerList == null || !config.terrorPlayerList.containsKey(playerUuid)) {
            MutableText welcomeMessage = Text.literal("Bienvenido. Este servidor tiene mecánicas de terror opcionales. Por favor, elige una opción:\n");

            MutableText joinText = Text.literal("[¡Sí, quiero participar!]");
            joinText.setStyle(Style.EMPTY
                    .withFormatting(Formatting.GREEN, Formatting.BOLD)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/terror whitelist join"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Haz clic para PARTICIPAR en las mecánicas de terror."))));

            MutableText declineText = Text.literal("   [No, gracias.]");
            declineText.setStyle(Style.EMPTY
                    .withFormatting(Formatting.RED)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/terror whitelist decline"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Haz clic para NO PARTICIPAR en las mecánicas de terror."))));

            MutableText decideLaterText = Text.literal("   [Decidiré luego]");
            decideLaterText.setStyle(Style.EMPTY
                    .withFormatting(Formatting.GRAY)
                    // No click event, simply closes chat, message will reappear on next login.
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Cierra el chat. Verás este mensaje de nuevo al unirte."))));

            // Optional: Add a command to reset preference if they accidentally click No.
            // This is now handled by /terror whitelist undecided
            // MutableText resetText = Text.literal("\nSi cambias de opinión, usa `/terror whitelist undecided` para ver este mensaje de nuevo.");
            // resetText.setStyle(Style.EMPTY.withFormatting(Formatting.YELLOW));

            player.sendMessage(welcomeMessage.append(joinText).append(declineText).append(decideLaterText), false);
        }
        // If player's UUID is in the map, they have already chosen (JOINED or DECLINED), so no message is shown.
    }

    @Override
    public void onEndTick(ServerWorld serverWorld) {
        if (serverWorld.getPlayers(VALID_PLAYER_PREDICATE).isEmpty()) {
            return;
        }

        if (!TheManEntity.isInAllowedDimension(serverWorld)) {
            return;
        }

        // Initial checks, moved up before spawn chance calculation
        if (TheManUtils.manExists(serverWorld) || TheManUtils.hallucinationsExists(serverWorld)) {
            return;
        }

        // Day check: if day spawning is disabled and it's day, return early.
        // If day spawning is enabled, the multiplier will be applied later.
        if (Util.isDay(serverWorld) && !Config.get().spawnInDay) {
            return;
        }

        if (--ticksBetweenSpawnAttempts <= 0L) {
            int spawnChanceScalesWithPlayerCountMultiplier = Config.get().spawnChanceScalesWithPlayerCount ? serverWorld.getPlayers(VALID_PLAYER_PREDICATE).size() : 1;

            if (serverWorld.getRegistryKey() == ModDimensions.ENSHROUDED_LEVEL_KEY) {
                spawnChanceScalesWithPlayerCountMultiplier *= 2; // This seems like a dimension-specific base multiplier, keeping it.
            }

            double spawnChance = Config.get().spawnChance * spawnChanceScalesWithPlayerCountMultiplier;

            // Apply day spawn multiplier
            if (Config.get().spawnInDay && Util.isDay(serverWorld)) {
                spawnChance *= Config.get().daySpawnChanceMultiplier;
            }

            // Apply moon event multipliers
            EnhancedCelestialsWorldData worldData = EnhancedCelestialsWorldData.get(serverWorld);
            if (worldData != null) {
                EnhancedCelestialsContext lunarContext = worldData.getLunarContext();
                if (lunarContext != null) {
                    LunarForecast forecast = lunarContext.getLunarForecast();
                    if (forecast != null) {
                        Optional<ResourceKey<LunarEvent>> eventKeyOptional = forecast.getCurrentEventRaw().getKey();
                        if (eventKeyOptional.isPresent()) {
                            // Use .location() for ResourceKey to get the Identifier
                            String eventIdString = eventKeyOptional.get().location().toString();
                            if (Config.get().moonEventSpawnMultipliers.containsKey(eventIdString)) {
                                spawnChance *= Config.get().moonEventSpawnMultipliers.get(eventIdString);
                                ManFromTheFog.LOGGER.info("Applying multiplier for event: " + eventIdString + ", new chance: " + spawnChance); // Optional: logging
                            }
                        }
                    }
                }
            }

            // If spawn chance becomes zero or less after multipliers, no need to proceed.
            if (spawnChance <= 0) {
                ticksBetweenSpawnAttempts = Util.secToTick(Config.get().timeBetweenSpawnAttempts);
                return;
            }

            double ambientChance = Config.get().fakeSpawnChance;

            double spawnRandom = Math.random();
            double ambientRandom = Math.random();

            if (spawnRandom < spawnChance) {
                if (ambientRandom < ambientChance) {
                    playCreepySoundAtRandomLocation(serverWorld,this.random);
                } else {
                    spawnManAtRandomLocation(serverWorld,this.random);
                }
            }

            ticksBetweenSpawnAttempts = Util.secToTick(Config.get().timeBetweenSpawnAttempts);
        }
    }
}
