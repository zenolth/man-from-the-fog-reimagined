package dev.zenolth.the_fog.common.status_effect;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModStatusEffects {
    public static final StatusEffect PARANOIA = new ParanoiaStatusEffect();

    public static void register() {
        Registry.register(Registries.STATUS_EFFECT, FogMod.id("paranoia"),PARANOIA);
    }
}
