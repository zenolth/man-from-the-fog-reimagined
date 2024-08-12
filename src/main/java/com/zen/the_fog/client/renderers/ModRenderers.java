package com.zen.the_fog.client.renderers;

import com.zen.the_fog.common.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModRenderers {

    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.THE_MAN, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_HALLUCINATION, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_PARANOIA, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_SPIT, TheManSpitRenderer::new);
    }
}
