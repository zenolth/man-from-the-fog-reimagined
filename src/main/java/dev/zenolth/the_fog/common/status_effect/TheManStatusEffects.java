package dev.zenolth.the_fog.common.status_effect;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class TheManStatusEffects {
    public static final StatusEffectInstance DARKNESS = new StatusEffectInstance(
            StatusEffects.DARKNESS,
            TheManEntity.STATUS_EFFECT_DURATION,
            0,
            false,
            false
    );
    public static final StatusEffectInstance NIGHT_VISION = new StatusEffectInstance(
            StatusEffects.NIGHT_VISION,
            TheManEntity.STATUS_EFFECT_DURATION,
            0,
            false,
            false
    );
    public static final StatusEffectInstance SPEED = new StatusEffectInstance(
            StatusEffects.SPEED,
            TheManEntity.STATUS_EFFECT_DURATION,
            1,
            false,
            false
    );
    public static final StatusEffectInstance REGENERATION = new StatusEffectInstance(
            StatusEffects.REGENERATION,
            StatusEffectInstance.INFINITE,
            2,
            false,
            false
    );

    public static final StatusEffectInstance POISON = new StatusEffectInstance(
            StatusEffects.POISON,
            200,
            1,
            false,
            true
    );
    public static final StatusEffectInstance SLOWNESS = new StatusEffectInstance(
            StatusEffects.SLOWNESS,
            300,
            1,
            false,
            true
    );
}
