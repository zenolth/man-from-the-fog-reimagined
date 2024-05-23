package com.zen.fogman;

import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.item.ModItems;
import com.zen.fogman.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManFromTheFog implements ModInitializer {
	public static final String MOD_ID = "man";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModSounds.registerSounds();
		ModItems.registerModItems();
		ModEntities.registerEntities();
	}
}