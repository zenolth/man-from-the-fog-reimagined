package dev.zenolth.the_fog.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.item.ModItems;
import dev.zenolth.the_fog.common.util.*;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class RenderEvents implements ClientLifecycleEvents.ClientStarted,HudRenderCallback, WorldRenderEvents.Last {
    private static RenderEvents INSTANCE;

    private static final Identifier VIGNETTE_TEXTURE = FogMod.id("textures/misc/vignette.png");

    public static RenderEvents getInstance() {
        if (INSTANCE == null) INSTANCE = new RenderEvents();
        return INSTANCE;
    }

    public void resetContext(DrawContext context) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        context.setShaderColor(1f,1f,1f,1f);
    }

    private void renderVignette(DrawContext context,float scale,float red,float green,float blue,float alpha) {
        alpha = MathHelper.clamp(alpha,0f,1f);

        if (alpha <= 0f) {
            this.resetContext(context);
            return;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        context.setShaderColor(red, green, blue, alpha);

        int originalWidth = context.getScaledWindowWidth();
        int originalHeight = context.getScaledWindowHeight();

        int scaledWidth = Math.round(originalWidth * scale);
        int scaledHeight = Math.round(originalHeight * scale);

        context.drawTexture(VIGNETTE_TEXTURE, -(scaledWidth - originalWidth) / 2, -(scaledHeight - originalHeight) / 2, -90, 0.0F, 0.0F, scaledWidth, scaledHeight, scaledWidth, scaledHeight);

        this.resetContext(context);
    }

    private void renderVignette(DrawContext context,float red,float green,float blue,float alpha) {
        this.renderVignette(context,1f,red,green,blue,alpha);
    }

    private float orbVignetteAlpha = 0f;
    private void renderOrbVignette(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || client.world == null) {
            this.hideVignetteAlpha = 0f;
            this.resetContext(context);
            return;
        }

        var trinketComponent = TrinketsApi.getTrinketComponent(player);

        if ((trinketComponent.isEmpty() || !trinketComponent.get().isEquipped(ModItems.EREBUS_ORB))) {
            this.orbVignetteAlpha = GeometryHelper.interpolate(this.orbVignetteAlpha,0f, (float) (1f - Math.pow(0.5f,tickDelta * 0.05f)));
        } else {

            if (WorldHelper.isSuperBloodMoon(client.world)) {
                var windowWidth = context.getScaledWindowWidth();
                var windowHeight = context.getScaledWindowHeight();

                var text = new StringBuilder("this will not help you");

                for (int i = 0; i < text.length(); i++) {
                    var c = text.charAt(i);

                    if (RandomNum.nextFloat() < 0.35f) {
                        c = Character.toUpperCase(c);
                    } else {
                        c = Character.toLowerCase(c);
                    }

                    text.setCharAt(i,c);
                }

                context.drawCenteredTextWithShadow(
                        client.textRenderer,
                        Text.of(text.toString()),
                        windowWidth / 2,
                        windowHeight - 50,
                        ColorHelper.Argb.getArgb(255,255,0,0)
                );
            }

            this.orbVignetteAlpha = GeometryHelper.interpolate(this.orbVignetteAlpha,1f, (float) (1f - Math.pow(0.5f,tickDelta * 0.13f)));
        }

        this.renderVignette(context,0.333f,0.169f,0.471f,this.orbVignetteAlpha * 0.75f);
    }

    private float hideVignetteAlpha = 0f;
    private float hideVignetteScale = 4f;
    private void renderHideVignette(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            this.hideVignetteAlpha = 0f;
            this.resetContext(context);
            return;
        }

        if (PlayerHelper.isHidden(player)) {
            var alpha = (float) (1f - Math.pow(0.5f,tickDelta * 0.1f));
            this.hideVignetteAlpha = GeometryHelper.interpolate(this.hideVignetteAlpha,0.6f, alpha);
            this.hideVignetteScale = GeometryHelper.interpolate(this.hideVignetteScale,1f, alpha);
        } else {
            var alpha = (float) (1f - Math.pow(0.5f,tickDelta * 0.3f));
            this.hideVignetteAlpha = GeometryHelper.interpolate(this.hideVignetteAlpha,0f, alpha);
            this.hideVignetteScale = GeometryHelper.interpolate(this.hideVignetteScale,2f, alpha);
        }

        this.renderVignette(context,this.hideVignetteScale,0.686f,0.878f,0.875f,this.hideVignetteAlpha);
    }

    @Override
    public void onClientStarted(MinecraftClient minecraftClient) {

    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            if (WorldHelper.isOnSurface(client.world,client.player)) {
                context.drawTextWithShadow(client.textRenderer,Text.of("On surface"),40,40,Colors.WHITE);
            } else {
                context.drawTextWithShadow(client.textRenderer,Text.of("In cave/Not on surface"),40,40,Colors.WHITE);
            }
        }

        this.renderOrbVignette(context,tickDelta);
        this.renderHideVignette(context,tickDelta);
    }

    @Override
    public void onLast(WorldRenderContext context) {

    }
}
