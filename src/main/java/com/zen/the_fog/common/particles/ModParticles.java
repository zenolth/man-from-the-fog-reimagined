package com.zen.the_fog.common.particles;

import com.zen.the_fog.common.ManFromTheFog;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {

    public static final DefaultParticleType THE_MAN_SPIT_PARTICLE = FabricParticleTypes.simple(true);

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(ManFromTheFog.MOD_ID,"the_man_spit_particle"),THE_MAN_SPIT_PARTICLE);
    }
}
