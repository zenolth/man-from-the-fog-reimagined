package com.zen.fogman.common.item.custom;

import com.zen.fogman.common.ManFromTheFog;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ClawsItemRenderer extends GeoItemRenderer<ClawsItem> {
    public ClawsItemRenderer() {
        super(new GeoModel<>() {
            @Override
            public Identifier getModelResource(ClawsItem animatable) {
                return new Identifier(ManFromTheFog.MOD_ID,"geo/item/claws.geo.json");
            }

            @Override
            public Identifier getTextureResource(ClawsItem animatable) {
                return new Identifier(ManFromTheFog.MOD_ID,"textures/item/claws.png");
            }

            @Override
            public Identifier getAnimationResource(ClawsItem animatable) {
                return new Identifier(ManFromTheFog.MOD_ID,"animations/item/claws.animation.json");
            }
        });
    }
}
