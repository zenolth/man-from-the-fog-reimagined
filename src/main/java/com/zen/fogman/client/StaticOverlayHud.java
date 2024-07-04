package com.zen.fogman.client;

import com.zen.fogman.ManFromTheFog;
import com.zen.fogman.entity.ModEntities;
import com.zen.fogman.entity.custom.TheManEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

public class StaticOverlayHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.player == null || client.isPaused()) {
            return;
        }

        List<TheManEntity> theManEntities = client.world.getEntitiesByType(
                ModEntities.THE_MAN,
                Box.of(
                        client.player.getPos(),
                        TheManEntity.MAN_CHASE_DISTANCE,
                        TheManEntity.MAN_CHASE_DISTANCE,
                        TheManEntity.MAN_CHASE_DISTANCE
                ),
                EntityPredicates.VALID_ENTITY
        );

        if (theManEntities.isEmpty()) {
            return;
        }

        for (TheManEntity theMan : theManEntities) {
            if (!theMan.isChasing()) {
                return;
            }
        }

        int width = drawContext.getScaledWindowWidth() * 4;
        int height = drawContext.getScaledWindowHeight() * 4;

        drawContext.drawTexture(
                new Identifier(ManFromTheFog.MOD_ID,"textures/hud/vignettechase.png"),
                0,0,0,
                0,0,
                width,height,
                700,700
        );
    }
}
