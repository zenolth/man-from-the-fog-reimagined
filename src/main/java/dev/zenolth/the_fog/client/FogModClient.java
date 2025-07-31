package dev.zenolth.the_fog.client;

import dev.zenolth.the_fog.client.events.ModClientEvents;
import dev.zenolth.the_fog.client.hud.RenderEvents;
import dev.zenolth.the_fog.client.rendering.ModRenderers;
import dev.zenolth.the_fog.common.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.particle.SpitParticle;

@Environment(EnvType.CLIENT)
public class FogModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(ModParticles.THE_MAN_SPIT_PARTICLE, SpitParticle.Factory::new);

		ModRenderers.registerRenderers();

		ClientTickEvents.END_CLIENT_TICK.register(ModClientEvents.getInstance());

		ClientLifecycleEvents.CLIENT_STARTED.register(RenderEvents.getInstance());
		HudRenderCallback.EVENT.register(RenderEvents.getInstance());
		WorldRenderEvents.LAST.register(RenderEvents.getInstance());
	}
}