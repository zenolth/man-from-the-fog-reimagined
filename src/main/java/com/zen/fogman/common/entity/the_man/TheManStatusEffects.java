package com.zen.fogman.common.entity.the_man;

import com.zen.fogman.common.other.Util;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.HashSet;

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
    public static final StatusEffectInstance DOLPHINS_GRACE = new StatusEffectInstance(
            StatusEffects.DOLPHINS_GRACE,
            StatusEffectInstance.INFINITE,
            1,
            false,
            false
    );

    public static final StatusEffectInstance GLOWING = new StatusEffectInstance(StatusEffects.GLOWING,50);

    public static final HashSet<StatusEffectInstance> EMPTY_STATUS_EFFECT_COLLECTION = new HashSet<>();
}
