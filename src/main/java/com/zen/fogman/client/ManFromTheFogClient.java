package com.zen.fogman.client;

import com.zen.fogman.client.events.ModClientEvents;
import com.zen.fogman.client.renderers.ModRenderers;
import com.zen.fogman.common.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.SpitParticle;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModClientEvents clientEvents = new ModClientEvents();

		ParticleFactoryRegistry.getInstance().register(ModParticles.THE_MAN_SPIT_PARTICLE, SpitParticle.Factory::new);

		ModRenderers.registerRenderers();

		ClientTickEvents.END_CLIENT_TICK.register(clientEvents);
	}
}