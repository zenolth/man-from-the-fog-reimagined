package com.zen.fogman;

import com.zen.fogman.entity.ModRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModRenderers.registerRenderers();
	}
}