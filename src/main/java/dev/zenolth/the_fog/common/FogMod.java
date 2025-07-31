package dev.zenolth.the_fog.common;

import dev.zenolth.the_fog.common.block.ModBlockEntityTypes;
import dev.zenolth.the_fog.common.block.ModBlocks;
import dev.zenolth.the_fog.common.compat.DummyECService;
import dev.zenolth.the_fog.common.compat.EnhancedCelestialsService;
import dev.zenolth.the_fog.common.components.WorldComponent;
import dev.zenolth.the_fog.common.config.ModConfig;
import dev.zenolth.the_fog.common.damage_type.ModDamageTypes;
import dev.zenolth.the_fog.common.entity.ModEntities;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.item.ModItems;
import dev.zenolth.the_fog.common.server.ServerEvents;
import dev.zenolth.the_fog.common.server.chat.ChatLog;
import dev.zenolth.the_fog.common.util.Console;
import dev.zenolth.the_fog.common.particles.ModParticles;
import dev.zenolth.the_fog.common.server.ModCommands;
import dev.zenolth.the_fog.common.server.ModNetworking;
import dev.zenolth.the_fog.common.services.ForecastService;
import dev.zenolth.the_fog.common.sounds.ModSounds;
import dev.zenolth.the_fog.common.status_effect.ModStatusEffects;
import dev.zenolth.the_fog.common.util.WorldHelper;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FogMod implements ModInitializer {
	public static final String MOD_NAME = "Man From The Fog Reimagined";
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static boolean DEBUG = true; // FIXME: Make sure to set this to false when commiting, or else The Man will be highlighted, and we don't want that, don't we?

	public static ModConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModConfig::new);

	public static ForecastService FORECAST = new DummyECService();

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID,path);
	}

	@Nullable
	public static TheManEntity getTheMan(World world) {
		var id = WorldComponent.get(world).theManId();
		if (id.isEmpty()) return null;

		var entity = world.getEntityById(id.get());
		if (entity instanceof TheManEntity theMan) return theMan;
		return null;
	}

	@Override
	public void onInitialize() {
		Console.writeln("Initializing %s",MOD_NAME);

		if (DEBUG) Console.writeln("Debug mode is active.",Console.Severity.WARNING);

		ModCommands.register();
		ModStatusEffects.register();
		ModDamageTypes.register();
		ModSounds.register();
		ModBlocks.register();
		ModBlockEntityTypes.initialize();
		ModItems.register();
		ModParticles.register();
		ModEntities.register();

		if (WorldHelper.isEnhancedCelestialsPresent()) {
			Console.writeln("Enhanced Celestials present, enabling compat.");
			FORECAST = new EnhancedCelestialsService();
		}

		CustomPortalBuilder.beginPortal()
				.frameBlock(ModBlocks.BLEEDING_OBSIDIAN)
				.onlyLightInOverworld()
				.lightWithItem(ModItems.TEAR_OF_THE_MAN)
				.destDimID(FogMod.id("enshrouded"))
				.customPortalBlock(ModBlocks.ENSHROUDED_PORTAL)
				.registerPortal();

		Console.writeln("Registering Events");

		ModNetworking.registerReceivers();
		ServerEntityEvents.ENTITY_LOAD.register(ServerEvents.getInstance());
		ServerWorldEvents.LOAD.register(ServerEvents.getInstance());

		ServerTickEvents.END_SERVER_TICK.register(ChatLog.getInstance());
		ServerMessageEvents.CHAT_MESSAGE.register(ChatLog.getInstance());

		ServerTickEvents.END_SERVER_TICK.register(ServerEvents.getInstance());
		ServerTickEvents.END_WORLD_TICK.register(ServerEvents.getInstance());

		ServerPlayConnectionEvents.JOIN.register(ServerEvents.getInstance());
		ServerPlayConnectionEvents.DISCONNECT.register(ServerEvents.getInstance());

		Console.writeln("Successfully initialized %s",MOD_NAME);
	}
}