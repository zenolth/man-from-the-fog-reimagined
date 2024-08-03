package com.zen.fogman.client.models;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

public class TheManSpitModel<T extends Entity> extends SinglePartEntityModel<T> {
    private static final String MAIN = "main";
    private final ModelPart root;

    public TheManSpitModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        int i = 2;
        modelPartData.addChild(
                "main",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(0.0F, -4.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(0.0F, 0.0F, -4.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(2.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(0.0F, 2.0F, 0.0F, 2.0F, 2.0F, 2.0F)
                        .cuboid(0.0F, 0.0F, 2.0F, 2.0F, 2.0F, 2.0F),
                ModelTransform.NONE
        );
        return TexturedModelData.of(modelData, 64, 32);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }
}
