package com.zen.the_fog.common;

import com.zen.the_fog.common.block.ModBlocks;
import com.zen.the_fog.common.damage_type.ModDamageTypes;
import com.zen.the_fog.common.entity.ModEntities;
import com.zen.the_fog.common.gamerules.ModGamerules;
import com.zen.the_fog.common.item.ModItems;
import com.zen.the_fog.common.particles.ModParticles;
import com.zen.the_fog.common.server.ModWorldEvents;
import com.zen.the_fog.common.server.ModNetworking;
import com.zen.the_fog.common.sounds.ModSounds;
import com.zen.the_fog.common.status_effects.ModStatusEffects;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModWorldEvents worldEvents = new ModWorldEvents();

		LOGGER.info("Initializing %s".formatted(MOD_ID));

		ModGamerules.register();
		ModStatusEffects.register();
		ModDamageTypes.register();
		ModSounds.register();
		ModBlocks.register();
		ModItems.register();
		ModParticles.register();
		ModEntities.register();

		/*BiomeModifications.create(new Identifier(MOD_ID,"setfog")).add(ModificationPhase.POST_PROCESSING,
				(context) -> true,
				ctx -> {
					ctx.getEffects().setFogColor(Colors.BLACK);
				}
		);*/

		CustomPortalBuilder.beginPortal()
				.frameBlock(ModBlocks.BLEEDING_OBSIDIAN)
				.onlyLightInOverworld()
				.lightWithItem(ModItems.TEAR_OF_THE_MAN)
				.destDimID(new Identifier(MOD_ID,"enshrouded"))
				.customPortalBlock(ModBlocks.ENSHROUDED_PORTAL)
				.registerPortal();

		ManFromTheFog.LOGGER.info("Registering Events");

		ModNetworking.registerReceivers();
		ServerEntityEvents.ENTITY_LOAD.register(worldEvents);
		ServerWorldEvents.LOAD.register(worldEvents);
		ServerTickEvents.END_WORLD_TICK.register(worldEvents);
		ServerPlayConnectionEvents.DISCONNECT.register(worldEvents);

		ManFromTheFog.LOGGER.info("Successfully initialized %s".formatted(MOD_ID));
	}
}