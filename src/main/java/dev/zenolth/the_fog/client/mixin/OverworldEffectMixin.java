package dev.zenolth.the_fog.client.mixin;

import dev.zenolth.the_fog.client.events.ModClientEvents;
import net.minecraft.client.render.DimensionEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.Overworld.class)
public class OverworldEffectMixin {
    @Inject(method = "useThickFog",at = @At("RETURN"),cancellable = true)
    public void useThickFogMixin(int camX, int camY, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ModClientEvents.getInstance().hasThickFog());
    }
}
