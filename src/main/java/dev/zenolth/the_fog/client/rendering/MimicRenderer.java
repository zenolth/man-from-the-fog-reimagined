package dev.zenolth.the_fog.client.rendering;

import dev.zenolth.the_fog.common.entity.mimic.MimicEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MimicRenderer extends LivingEntityRenderer<MimicEntity, PlayerEntityModel<MimicEntity>> {
    private final PlayerEntityModel<MimicEntity> normalModel;
    private final PlayerEntityModel<MimicEntity> slimModel;

    public MimicRenderer(EntityRendererFactory.Context ctx) {
        super(
                ctx,
                new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false),
                0.5F
        );

        this.normalModel = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false);
        this.slimModel = new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true);

        this.model = this.normalModel;

        this.addFeature(new StuckArrowsFeatureRenderer<>(ctx, this));
        this.addFeature(new HeadFeatureRenderer<>(this, ctx.getModelLoader(), ctx.getHeldItemRenderer()));
        this.addFeature(new ElytraFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new TridentRiptideFeatureRenderer<>(this, ctx.getModelLoader()));
        this.addFeature(new StuckStingersFeatureRenderer<>(this));
    }

    @Override
    public void render(MimicEntity mimic, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        var client = MinecraftClient.getInstance();

        var mimickedPlayerUUID = mimic.mimickedPlayerUUID.get();
        if (mimickedPlayerUUID.isPresent() && client.player != null) {
            var playerListEntry = client.player.networkHandler.getPlayerListEntry(mimickedPlayerUUID.get());
            var playerModel = playerListEntry == null ? DefaultSkinHelper.getModel(mimickedPlayerUUID.get()) : playerListEntry.getModel();
            if (Objects.equals(playerModel, "slim")) {
                if (this.model != this.slimModel) this.model = this.slimModel;
            } else {
                if (this.model != this.normalModel) this.model = this.normalModel;
            }
        }

        super.render(mimic, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(MimicEntity entity) {
        var mimickedPlayerUUID = entity.mimickedPlayerUUID.get();
        if (mimickedPlayerUUID.isEmpty()) {
            return DefaultSkinHelper.getTexture();
        }
        var client = MinecraftClient.getInstance();
        if (client.player == null) return DefaultSkinHelper.getTexture(mimickedPlayerUUID.get());
        var playerListEntry = client.player.networkHandler.getPlayerListEntry(mimickedPlayerUUID.get());
        return playerListEntry == null ? DefaultSkinHelper.getTexture(mimickedPlayerUUID.get()) : playerListEntry.getSkinTexture();
    }
}
