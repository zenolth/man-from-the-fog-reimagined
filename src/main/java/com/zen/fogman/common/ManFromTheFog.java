package com.zen.fogman.common;

import com.zen.fogman.common.entity.ModEntities;
import com.zen.fogman.common.gamerules.ModGamerules;
import com.zen.fogman.common.item.ModItems;
import com.zen.fogman.common.server.ModWorldEvents;
import com.zen.fogman.common.server.ModNetworking;
import com.zen.fogman.common.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModWorldEvents worldEvents = new ModWorldEvents();

		LOGGER.info("Initializing %s".formatted(MOD_ID));

		ModGamerules.registerGamerules();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModEntities.registerEntities();

		ManFromTheFog.LOGGER.info("Registering Events");
		ModNetworking.registerReceivers();
		ServerEntityEvents.ENTITY_LOAD.register(worldEvents);
		ServerWorldEvents.LOAD.register(worldEvents);
		ServerTickEvents.END_WORLD_TICK.register(worldEvents);
		ManFromTheFog.LOGGER.info("Successfully initialized %s".formatted(MOD_ID));
	}
}