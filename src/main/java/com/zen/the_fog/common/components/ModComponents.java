package com.zen.the_fog.common.components;

import com.zen.the_fog.common.ManFromTheFog;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public final class ModComponents implements WorldComponentInitializer {
    public static final ComponentKey<TheManHealthComponent> THE_MAN_HEALTH =
            ComponentRegistry.getOrCreate(Identifier.of(ManFromTheFog.MOD_ID,"the_man_health"), TheManHealthComponent.class);

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(THE_MAN_HEALTH, it -> new TheManHealthComponent());
    }
}
