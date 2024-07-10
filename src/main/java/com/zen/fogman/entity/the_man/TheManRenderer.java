package com.zen.fogman.entity.the_man;

import com.zen.fogman.ManFromTheFog;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

@Environment(EnvType.CLIENT)
public class TheManRenderer extends GeoEntityRenderer<TheManEntity> {
    public TheManRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new DefaultedEntityGeoModel<>(new Identifier(ManFromTheFog.MOD_ID,"fogman"),false)
        );

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
