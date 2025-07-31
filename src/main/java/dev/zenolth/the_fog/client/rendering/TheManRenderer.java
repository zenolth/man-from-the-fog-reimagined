package dev.zenolth.the_fog.client.rendering;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
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
                new DefaultedEntityGeoModel<>(FogMod.id("the_man"))
        );

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public Identifier getTexture(TheManEntity animatable) {
        return super.getTexture(animatable);
    }
}
