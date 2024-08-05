package com.zen.fogman.client.renderers;

import com.zen.fogman.common.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;

public class ModRenderers {

    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.THE_MAN, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_HALLUCINATION, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_PARANOIA, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_SPIT, TheManSpitRenderer::new);
    }
}
