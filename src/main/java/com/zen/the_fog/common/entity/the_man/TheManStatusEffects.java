package com.zen.the_fog.common.entity.the_man;

import com.zen.the_fog.common.other.Util;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class TheManStatusEffects {
    public static final StatusEffectInstance DARKNESS = new StatusEffectInstance(
            StatusEffects.DARKNESS,
            Util.secToTick(10.0),
            0,
            false,
            false
    );
    public static final StatusEffectInstance NIGHT_VISION = new StatusEffectInstance(
            StatusEffects.NIGHT_VISION,
            Util.secToTick(10.0),
            0,
            false,
            false
    );
    public static final StatusEffectInstance SPEED = new StatusEffectInstance(
            StatusEffects.SPEED,
            Util.secToTick(10.0),
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
}
