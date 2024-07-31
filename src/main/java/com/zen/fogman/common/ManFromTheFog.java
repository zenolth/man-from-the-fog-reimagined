package com.zen.fogman.common;

import com.zen.fogman.common.block.ModBlocks;
import com.zen.fogman.common.damage_type.ModDamageTypes;
import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.gamerules.ModGamerules;
import com.zen.fogman.common.item.ModItems;
import com.zen.fogman.common.server.ModWorldEvents;
import com.zen.fogman.common.server.ModNetworking;
import com.zen.fogman.common.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.dimension.FabricDimensionInternals;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
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
		ModDamageTypes.register();
		ModSounds.register();
		ModBlocks.register();
		ModItems.register();
		ModEntities.register();

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