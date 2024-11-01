package com.zen.the_fog.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zen.the_fog.common.item.ModItems;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class HudEvents implements HudRenderCallback {
    private static final Identifier VIGNETTE_TEXTURE = new Identifier("minecraft","textures/misc/vignette.png");

    private float vignetteAlpha = 0f;

    public void resetContext(DrawContext context) {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        context.setShaderColor(1f,1f,1f,1f);
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || (TrinketsApi.getTrinketComponent(player).isEmpty() || !TrinketsApi.getTrinketComponent(player).get().isEquipped(ModItems.EREBUS_ORB))) {
            if (this.vignetteAlpha > 0) {
                this.vignetteAlpha -= tickDelta * 0.05f;
            }
        } else {
            if (this.vignetteAlpha < 1) {
                this.vignetteAlpha += tickDelta * 0.13f;
            }
        }

        this.vignetteAlpha = MathHelper.clamp(this.vignetteAlpha,0f,1f);

        if (this.vignetteAlpha <= 0f) {
            this.resetContext(context);
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        /*RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );*/
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE
        );

        context.setShaderColor(0.333f, 0.169f, 0.471f, this.vignetteAlpha);

        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();

        context.drawTexture(VIGNETTE_TEXTURE, 0, 0, -90, 0.0F, 0.0F, scaledWidth, scaledHeight, scaledWidth, scaledHeight);

        this.resetContext(context);
    }
}
