package com.zen.the_fog.client;

import com.zen.the_fog.client.events.ModClientEvents;
import com.zen.the_fog.client.hud.HudEvents;
import com.zen.the_fog.client.renderers.ModRenderers;
import com.zen.the_fog.common.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.particle.SpitParticle;

@Environment(EnvType.CLIENT)
public class ManFromTheFogClient implements ClientModInitializer {

	public static final ModClientEvents CLIENT_EVENTS = new ModClientEvents();
	public static final HudEvents HUD_EVENTS = new HudEvents();

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.THE_MAN_SPIT_PARTICLE, SpitParticle.Factory::new);

		ModRenderers.registerRenderers();

		ClientTickEvents.END_CLIENT_TICK.register(CLIENT_EVENTS);
		HudRenderCallback.EVENT.register(HUD_EVENTS);
	}
}