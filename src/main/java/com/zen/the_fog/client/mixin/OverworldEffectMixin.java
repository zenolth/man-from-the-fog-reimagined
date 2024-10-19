package com.zen.the_fog.client.mixin;

import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.Overworld.class)
public class OverworldEffectMixin {
    @Inject(method = "useThickFog",at = @At("RETURN"),cancellable = true)
    public void useThickFogMixin(int camX, int camY, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
