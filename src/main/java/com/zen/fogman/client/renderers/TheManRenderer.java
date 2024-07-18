package com.zen.fogman.client.renderers;

import com.zen.fogman.common.ManFromTheFog;
import com.zen.fogman.common.entity.the_man.TheManEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
                new DefaultedEntityGeoModel<>(new Identifier(ManFromTheFog.MOD_ID,"fogman"))
        );

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
