package dev.zenolth.the_fog.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(SplashTextRenderer.class)
public class SplashTextRendererMixin {

    @Inject(method = "render",at = @At("HEAD"),cancellable = true)
    public void renderText(DrawContext context, int screenWidth, TextRenderer textRenderer, int alpha, CallbackInfo ci) {
        var renderer = (SplashTextRenderer)(Object) this;

        var lines = renderer.text.split("//");
        if (lines.length > 1) {
            var lineWidth = 0;

            for (var line : lines) {
                lineWidth += textRenderer.getWidth(line);
            }
            lineWidth /= lines.length;

            for (int i = 0; i < lines.length; i++) {
                var line = lines[i];

                float f = 1.8F - MathHelper.abs(MathHelper.sin((float) (Util.getMeasuringTimeMs() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
                f = f * 100.0F / (float) (lineWidth + 32);

                context.getMatrices().push();
                context.getMatrices().translate((float) screenWidth / 2.0F + 123.0F, 69.0F, 0.0F);
                context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F));
                context.getMatrices().translate(0, i * f * 10f, 0f);
                context.getMatrices().scale(f, f, f);
                context.drawCenteredTextWithShadow(textRenderer, line, 0, -8, 16776960 | alpha);
                context.getMatrices().pop();
            }

            ci.cancel();
        }
    }
}
