package com.zen.the_fog.client.mixin;

import com.zen.the_fog.client.mixin_interfaces.ClientPlayerEntityInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow public abstract void close();

    @Inject(method = "getBasicProjectionMatrix",at = @At("RETURN"),cancellable = true)
    public void projectionMatrixModifier(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        GameRenderer thisObject = (GameRenderer)(Object) this;

        if (thisObject.getClient().player == null || thisObject.getClient().world == null) {
            cir.setReturnValue(cir.getReturnValue());
            return;
        }

        Matrix4f projectionMatrix = cir.getReturnValue();

        float glitchMultiplier = (float) (((ClientPlayerEntityInterface) thisObject.getClient().player).the_fog_is_coming$getGlitchMultiplier() * thisObject.getClient().options.getFovEffectScale().getValue());

        projectionMatrix.scale(
                (float) (1.0 + Math.random() * 0.05 * glitchMultiplier),
                (float) (1.0 + Math.random() * 0.07 * glitchMultiplier),
                1f
        );

        cir.setReturnValue(projectionMatrix);
    }
}
