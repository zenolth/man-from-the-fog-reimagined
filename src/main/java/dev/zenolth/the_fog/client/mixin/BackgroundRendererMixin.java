package dev.zenolth.the_fog.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.zenolth.the_fog.client.events.ModClientEvents;
import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.util.GeometryHelper;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Unique
    private static final float FOG_INTERPOLATION_SPEED = 0.01f;

    @Unique
    private static final float NIGHT_FOG_END = 52f;

    @Unique
    private static BackgroundRenderer.FogData fogData;

    @Inject(method = "applyFog",at = @At(value = "TAIL"))
    private static void makeFogThiccer(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        ClientWorld world = (ClientWorld) entity.getWorld();

        if (world.getRegistryKey() != ClientWorld.OVERWORLD) {
            return;
        }

        if (fogData == null) fogData = new BackgroundRenderer.FogData(fogType);

        world.calculateAmbientDarkness();
        var darkness = MathHelper.clamp(Math.round(world.getAmbientDarkness() / 11.0f),0f,1f);

        var newFogEnd = (float) Math.round(GeometryHelper.interpolate(viewDistance,NIGHT_FOG_END * FogMod.CONFIG.miscellaneous.fogDensityMultiplier,darkness));
        var alpha = (float) (1f - Math.pow(0.5f,tickDelta * FOG_INTERPOLATION_SPEED));

        if (newFogEnd > viewDistance / 1.5f) {
            ModClientEvents.getInstance().setThickFog(thickFog);
        } else {
            ModClientEvents.getInstance().setThickFog(darkness > 0.5f);
        }

        fogData.fogStart = 0;
        fogData.fogEnd = MathHelper.lerp(alpha,fogData.fogEnd,newFogEnd);
        fogData.fogShape = ModClientEvents.getInstance().hasThickFog() ? FogShape.SPHERE : FogShape.CYLINDER;

        RenderSystem.setShaderFogStart(fogData.fogStart);
        RenderSystem.setShaderFogEnd(fogData.fogEnd);
        RenderSystem.setShaderFogShape(fogData.fogShape);
    }
}
