package com.zen.fogman.entity.custom;

import com.zen.fogman.ManFromTheFog;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TheManRenderer extends GeoEntityRenderer<TheManEntity> {
    public TheManRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new DefaultedEntityGeoModel<>(new Identifier(ManFromTheFog.MOD_ID,"fogman"),true)
        );
        scaleWidth = 8;
        scaleHeight = 8;
        addRenderLayer(new AutoGlowingGeoLayer(this));
    }
}
