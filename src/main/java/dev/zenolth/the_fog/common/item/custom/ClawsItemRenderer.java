package dev.zenolth.the_fog.common.item.custom;

import dev.zenolth.the_fog.common.FogMod;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ClawsItemRenderer extends GeoItemRenderer<ClawsItem> {
    public ClawsItemRenderer() {
        super(new GeoModel<>() {
            @Override
            public Identifier getModelResource(ClawsItem animatable) {
                return FogMod.id("geo/item/claws.geo.json");
            }

            @Override
            public Identifier getTextureResource(ClawsItem animatable) {
                return FogMod.id("textures/item/claws.png");
            }

            @Override
            public Identifier getAnimationResource(ClawsItem animatable) {
                return FogMod.id("animations/item/claws.animation.json");
            }
        });
    }
}
