package dev.zenolth.the_fog.client.rendering;

import dev.zenolth.the_fog.client.models.TheManSpitModel;
import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.entity.the_man.TheManSpitEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class TheManSpitRenderer extends EntityRenderer<TheManSpitEntity> {

    public static final Identifier TEXTURE = FogMod.id("textures/particle/the_man_spit.png");
    private final TheManSpitModel<TheManSpitEntity> model;

    protected TheManSpitRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new TheManSpitModel<>(ctx.getPart(EntityModelLayers.LLAMA_SPIT));
    }

    public void render(TheManSpitEntity theManSpitEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0F, 0.15F, 0.0F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(g, theManSpitEntity.prevYaw, theManSpitEntity.getYaw()) - 90.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(g, theManSpitEntity.prevPitch, theManSpitEntity.getPitch())));
        this.model.setAngles(theManSpitEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
        super.render(theManSpitEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(TheManSpitEntity entity) {
        return TEXTURE;
    }
}
