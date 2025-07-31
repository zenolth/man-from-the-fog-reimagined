package dev.zenolth.the_fog.common.particles;

import dev.zenolth.the_fog.common.FogMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModParticles {

    public static final DefaultParticleType THE_MAN_SPIT_PARTICLE = FabricParticleTypes.simple(true);

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, FogMod.id("the_man_spit_particle"),THE_MAN_SPIT_PARTICLE);
    }
}
