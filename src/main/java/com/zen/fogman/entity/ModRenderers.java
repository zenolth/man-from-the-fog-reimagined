package com.zen.fogman.entity;

import com.zen.fogman.entity.custom.TheManRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModRenderers {

    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.THE_MAN, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_HALLUCINATION, TheManRenderer::new);
    }
}
