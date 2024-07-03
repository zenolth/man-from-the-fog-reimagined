package com.zen.fogman;

import com.zen.fogman.client.ManClientTick;
import com.zen.fogman.client.StaticOverlayHud;
import com.zen.fogman.entity.ModRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		ModRenderers.registerRenderers();

		HudRenderCallback.EVENT.register(new StaticOverlayHud());
		ClientTickEvents.END_CLIENT_TICK.register(new ManClientTick());
	}
}