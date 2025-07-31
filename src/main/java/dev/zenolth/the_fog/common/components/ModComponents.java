package dev.zenolth.the_fog.common.components;

import dev.zenolth.the_fog.common.FogMod;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;

public final class ModComponents implements WorldComponentInitializer {
    public static final ComponentKey<WorldComponent> WORLD_COMPONENT =
            ComponentRegistry.getOrCreate(FogMod.id("world_component"), WorldComponent.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(WORLD_COMPONENT, WorldComponent::new);
    }
}
