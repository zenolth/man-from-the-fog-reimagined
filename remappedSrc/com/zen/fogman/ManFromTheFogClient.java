package com.zen.fogman;

import com.zen.fogman.client.ManClientTick;
import com.zen.fogman.common.entity.ModRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ModRenderers.registerRenderers();
		ClientTickEvents.END_CLIENT_TICK.register(new ManClientTick());
	}
}