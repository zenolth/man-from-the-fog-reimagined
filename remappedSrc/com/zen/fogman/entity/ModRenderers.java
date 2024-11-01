package com.zen.the_fog.common.entity;

import com.zen.the_fog.common.entity.the_man.TheManRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModRenderers {

    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.THE_MAN, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_HALLUCINATION, TheManRenderer::new);
    }
}
