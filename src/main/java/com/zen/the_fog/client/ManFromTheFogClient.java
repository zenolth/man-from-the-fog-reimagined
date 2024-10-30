package com.zen.the_fog.client;

import com.zen.the_fog.client.events.ModClientEvents;
import com.zen.the_fog.client.hud.HudEvents;
import com.zen.the_fog.client.networking.ClientNetworking;
import com.zen.the_fog.client.renderers.ModRenderers;
import com.zen.the_fog.common.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.particle.SpitParticle;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModClientEvents clientEvents = new ModClientEvents();
		HudEvents hudEvents = new HudEvents();

		ParticleFactoryRegistry.getInstance().register(ModParticles.THE_MAN_SPIT_PARTICLE, SpitParticle.Factory::new);

		ModRenderers.registerRenderers();
		ClientNetworking.registerReceivers();

		ClientTickEvents.END_CLIENT_TICK.register(clientEvents);
		ClientEntityEvents.ENTITY_LOAD.register(clientEvents);

		HudRenderCallback.EVENT.register(hudEvents);
	}
}