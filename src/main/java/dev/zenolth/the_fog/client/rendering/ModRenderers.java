package dev.zenolth.the_fog.client.rendering;

import dev.zenolth.the_fog.common.entity.ModEntities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ModRenderers {

    public static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.THE_MAN, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_HALLUCINATION, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_PARANOIA, TheManRenderer::new);
        EntityRendererRegistry.register(ModEntities.THE_MAN_SPIT, TheManSpitRenderer::new);

        EntityRendererRegistry.register(ModEntities.MIMIC,MimicRenderer::new);
    }
}
