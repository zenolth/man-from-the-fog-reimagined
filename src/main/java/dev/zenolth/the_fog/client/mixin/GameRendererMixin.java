package dev.zenolth.the_fog.client.mixin;

import dev.zenolth.the_fog.common.util.RandomNum;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
        MinecraftClient client = thisObject.getClient();

        if (client.player == null || client.world == null) {
            cir.setReturnValue(cir.getReturnValue());
            return;
        }

        Matrix4f projectionMatrix = cir.getReturnValue();

        float glitchMultiplier = (float) (client.player.the_fog_is_coming$getGlitchMultiplier() * client.options.getFovEffectScale().getValue());

        projectionMatrix.scale(
                1f + RandomNum.nextFloat() * 0.05f * glitchMultiplier,
                1f + RandomNum.nextFloat() * 0.07f * glitchMultiplier,
                1f
        );

        projectionMatrix.rotate((float) Math.toRadians(1.0 * RandomNum.next(-1.0,1.0) * glitchMultiplier),0f,0f,1f);

        projectionMatrix.mul3x3(
                1f,0f,0f,
                RandomNum.nextFloat() * 0.07f * glitchMultiplier,1f,0f,
                0f,0f,1f
        );

        //projectionMatrix.mul(1f,0f,0f,0f,1f,1f,0f,0f,0f,0f,1f,0f,0f,0f,0f,0f);

        cir.setReturnValue(projectionMatrix);
    }
}
