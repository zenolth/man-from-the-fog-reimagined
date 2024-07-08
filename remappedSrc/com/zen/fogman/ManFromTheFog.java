package com.zen.fogman;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.gamerules.ModGamerules;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.server.ManWorldEvents;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "the_fog_is_coming";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ManWorldEvents worldTick = new ManWorldEvents();

	@Override
	public void onInitialize() {
		ModGamerules.registerGamerules();
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModEntities.registerEntities();

		ServerWorldEvents.LOAD.register(worldTick);
		ServerTickEvents.END_WORLD_TICK.register(worldTick);
	}
}