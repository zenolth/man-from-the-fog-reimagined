package com.zen.fogman.common.status_effects;

import com.zen.fogman.common.ManFromTheFog;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModStatusEffects {
    public static final StatusEffect PARANOIA = new ParanoiaStatusEffect();

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT,new Identifier(ManFromTheFog.MOD_ID,"paranoia"),PARANOIA);
    }
}
